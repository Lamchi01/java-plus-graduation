package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.clients.UserClient;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.stats.protobuf.RecommendedEventProto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationMapper compilationMapper;
    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final StatClient statClient;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation newCompilation = new Compilation();
        newCompilation.setTitle(newCompilationDto.getTitle());
        newCompilation.setPinned(newCompilationDto.getPinned());

        if (newCompilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIsIn(newCompilationDto.getEvents());
            newCompilation.setEvents(events);
        }
        if (newCompilation.getEvents() == null) {
            return compilationMapper.toCompilationDto(compilationRepository.save(newCompilation), new ArrayList<>());
        }
        return compilationMapper.toCompilationDto(compilationRepository.save(newCompilation), getShortEventRecommendation(newCompilation.getEvents()));
    }

    @Override
    public void delete(Long id) {
        compilationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Compilation.class, "(Подборка) c ID = " + id + ", не найдена"));

        compilationRepository.deleteById(id);
    }

    @Override
    public CompilationDto update(Long id, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Compilation.class, "(Подборка) c ID = " + id + ", не найдена"));
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIsIn(updateCompilationRequest.getEvents());
            compilation.setEvents(events);
        }
        return compilationMapper.toCompilationDto(
                compilationRepository.save(compilation),
                getShortEventRecommendation(compilation.getEvents()));
    }

    @Override
    public Collection<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from, size);
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);

        List<Event> allEvents = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .toList();

        Map<Long, RecommendedEventProto> ratingsMap = statClient.getInteractionsCount(
                allEvents.stream().map(Event::getId).toList()
        ).collect(Collectors.toMap(
                RecommendedEventProto::getEventId,
                Function.identity()
        ));

        Set<Long> initiatorIds = allEvents.stream()
                .map(Event::getInitiator)
                .collect(Collectors.toSet());
        Map<Long, UserShortDto> initiatorsMap = userClient.getAllUsersShort(new ArrayList<>(initiatorIds)).stream()
                .collect(Collectors.toMap(
                        UserShortDto::getId,
                        Function.identity()
                ));

        return compilations.stream()
                .map(compilation -> compilationMapper.toCompilationDto(
                        compilation,
                        getShortEventRecommendation(
                                compilation.getEvents(),
                                ratingsMap,
                                initiatorsMap
                                )
                )).toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(Compilation.class, "Подборка событий не найдена"));

        return compilationMapper.toCompilationDto(compilation, getShortEventRecommendation(compilation.getEvents()));
    }

    private List<EventShortDto> getShortEventRecommendation(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }
        List<RecommendedEventProto> ratingList = statClient.getInteractionsCount(
                events.stream().map(Event::getId).toList()
        ).toList();
        List<UserShortDto> initiators = userClient.getAllUsersShort(events.stream()
                .map(Event::getInitiator)
                .collect(Collectors.toSet())
                .stream()
                .toList()
        );
        return eventMapper.mapToShortDto(events, initiators, ratingList);
    }

    private List<EventShortDto> getShortEventRecommendation(
            List<Event> events,
            Map<Long, RecommendedEventProto> ratingsMap,
            Map<Long, UserShortDto> initiatorsMap
    ) {
        if (events.isEmpty()) {
            return List.of();
        }

        List<RecommendedEventProto> ratingList = events.stream()
                .map(Event::getId)
                .map(ratingsMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<UserShortDto> initiators = events.stream()
                .map(Event::getInitiator)
                .map(initiatorsMap::get)
                .filter(Objects::nonNull)
                .toList();

        return eventMapper.mapToShortDto(events, initiators, ratingList);
    }
}