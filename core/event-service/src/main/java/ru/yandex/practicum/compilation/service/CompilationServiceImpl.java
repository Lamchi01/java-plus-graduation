package ru.yandex.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.mapper.CategoryMapper;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.category.repository.CategoryRepository;
import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.compilation.mapper.CompilationMapper;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.compilation.repository.CompilationRepository;
import ru.yandex.practicum.event.clients.UserClient;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.EntityNotFoundException;
import ru.yandex.practicum.user.dto.UserShortDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationMapper compilationMapper;
    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserClient userRepository;

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
        return compilationMapper.toCompilationDto(compilationRepository.save(newCompilation), getEvents(newCompilation.getEvents().stream().map(Event::getId).toList()));
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
                getEvents(compilation.getEvents().stream().map(Event::getId).toList()));
    }

    @Override
    public Collection<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from, size);
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);

        return compilations.stream()
                .map(compilation -> compilationMapper.toCompilationDto(
                        compilation,
                        getEvents(compilation.getEvents().stream().map(Event::getId).toList())))
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(Compilation.class, "Подборка событий не найдена"));

        return compilationMapper.toCompilationDto(compilation, getEvents(compilation.getEvents().stream().map(Event::getId).toList()));
    }

    private List<EventShortDto> getEvents(List<Long> eventsIds) {
        List<Event> events = eventRepository.findAllByIdIsIn(eventsIds);
        Map<Long, CategoryDto> categoryDtoMap = getCategories(events.stream().map(Event::getCategory).map(Category::getId).toList());
        Map<Long, UserShortDto> userShortDtoMap = getUsers(events.stream().map(Event::getInitiator).toList());
        return events.stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        userShortDtoMap.get(event.getInitiator()),
                        categoryDtoMap.get(event.getCategory().getId())))
                .toList();
    }

    private Map<Long, CategoryDto> getCategories(List<Long> ids) {
        List<Category> categories = categoryRepository.findAllById(ids);
        return categories.stream()
                .map(categoryMapper::toCategoryDto)
                .toList().stream().collect(Collectors.toMap(CategoryDto::getId, category -> category));
    }

    private Map<Long, UserShortDto> getUsers(List<Long> userIds) {
        return userRepository.getAllUsersShort(userIds).stream()
                .collect(Collectors.toMap(UserShortDto::getId, user -> user));
    }
}