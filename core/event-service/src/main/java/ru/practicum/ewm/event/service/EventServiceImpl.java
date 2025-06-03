package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.client.UserActionType;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.clients.RequestsClient;
import ru.practicum.ewm.event.clients.UserClient;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exception.ConditionNotMetException;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.exception.InitiatorRequestException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;
import ru.practicum.ewm.stats.protobuf.RecommendedEventProto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.utils.Constants.FORMAT_DATETIME;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final LocationRepository locationRepository;

    private final StatClient statClient;
    private final UserClient userClient;
    private final CategoryRepository categoryRepository;
    private final RequestsClient requestsClient;
    private final CommentRepository commentRepository;

    @Override
    public List<EventShortDto> getAllEvents(ReqParam reqParam) {
        Pageable pageable = PageRequest.of(reqParam.getFrom(), reqParam.getSize());

        if (reqParam.getRangeStart() == null || reqParam.getRangeEnd() == null) {
            reqParam.setRangeStart(LocalDateTime.now());
            reqParam.setRangeEnd(LocalDateTime.now().plusYears(1));
        }
        List<Event> events = eventRepository.findEvents(
                reqParam.getText(),
                reqParam.getCategories(),
                reqParam.getPaid(),
                reqParam.getRangeStart(),
                reqParam.getRangeEnd(),
                reqParam.getOnlyAvailable(),
                pageable);

        Map<Long, UserShortDto> usersMap = getUsersMap(events.stream().map(Event::getInitiator).toList());
        List<EventFullDto> eventFullDtos = events.stream()
                .map(e -> eventMapper.toEventFullDto(e, usersMap.get(e.getInitiator())))
                .toList();
        if (eventFullDtos.isEmpty()) {
            throw new ValidationException(ReqParam.class, " События не найдены");
        }
        List<Long> eventsIds = eventFullDtos.stream().map(EventFullDto::getId).toList();
        List<EventCommentCount> eventCommentCountList = commentRepository.findAllByEventIds(eventsIds);

        eventFullDtos.forEach(eventFullDto ->
                eventFullDto.setCommentsCount(eventCommentCountList.stream()
                        .filter(eventComment -> eventComment.getEventId().equals(eventFullDto.getId()))
                        .map(EventCommentCount::getCommentCount)
                        .findFirst()
                        .orElse(0L)
                )
        );

        List<EventShortDto> result = eventMapper.toEventShortDtos(eventFullDtos);
        Map<Long, Double> ratingMap = getRating(result.stream().map(EventShortDto::getId).toList());
        Map<Long, Long> requestsMap = getRequests(result.stream().map(EventShortDto::getId).toList());
        for (EventShortDto eventShortDto : result) {
            eventShortDto.setRating(ratingMap.getOrDefault(eventShortDto.getId(), 0.0));
            eventShortDto.setConfirmedRequests(requestsMap.getOrDefault(eventShortDto.getId(), 0L));
        }

        if (reqParam.getSort() != null) {
            return switch (reqParam.getSort()) {
                case EVENT_DATE ->
                        result.stream().sorted(Comparator.comparing(EventShortDto::getEventDate)).toList();
                case VIEWS ->
                        result.stream().sorted(Comparator.comparing(EventShortDto::getRating)).toList();
            };
        }
        return result;
    }

    @Override
    public List<EventFullDto> getAllEvents(AdminEventParams params) {
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize());

        if (params.getRangeStart() == null || params.getRangeEnd() == null) {
            params.setRangeStart(LocalDateTime.now());
            params.setRangeEnd(LocalDateTime.now().plusYears(1));
        }
        List<Event> events = eventRepository.findAdminEvents(
                params.getUsers(),
                params.getStates(),
                params.getCategories(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable);

        Map<Long, UserShortDto> usersMap = getUsersMap(events.stream().map(Event::getInitiator).toList());
        List<EventFullDto> result = events.stream()
                .map(event -> eventMapper.toEventFullDto(event, usersMap.get(event.getInitiator())))
                .toList();
        Map<Long, Double> ratingMap = getRating(result.stream().map(EventFullDto::getId).toList());
        Map<Long, Long> requestsMap = getRequests(result.stream().map(EventFullDto::getId).toList());
        for (EventFullDto eventFullDto : result) {
            eventFullDto.setRating(ratingMap.getOrDefault(eventFullDto.getId(), 0.0));
            eventFullDto.setConfirmedRequests(requestsMap.getOrDefault(eventFullDto.getId(), 0L));
        }
        return result;
    }

    @Override
    public EventFullDto publicGetEvent(long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие c ID - " + id + ", не найдено."));
        if (event.getState() != EventState.PUBLISHED) {
            throw new EntityNotFoundException(Event.class, " Событие c ID - " + id + ", ещё не опубликовано.");
        }
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, findShortUser(event.getInitiator()));
        eventFullDto.setCommentsCount(commentRepository.countCommentByEvent_Id(event.getId()));
        statClient.collectUserAction(eventFullDto.getInitiator().getId(), eventFullDto.getId(), UserActionType.VIEW);
        eventFullDto.setRating(getRating(List.of(eventFullDto.getId())).getOrDefault(eventFullDto.getId(), 0.0));
        eventFullDto.setConfirmedRequests(getRequests(eventFullDto.getId()));
        return eventFullDto;
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(),
                DateTimeFormatter.ofPattern(FORMAT_DATETIME));
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(NewEventDto.class, "До начала события осталось меньше двух часов");
        }
        UserShortDto initiator = userClient.getUserShortById(userId);
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new EntityNotFoundException(Category.class, "Категория не найден"));

        Event event = eventMapper.toEvent(newEventDto);
        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0L);
        }
        event.setInitiator(initiator.getId());
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setLocation(locationRepository.save(event.getLocation()));
        return eventMapper.toEventFullDto(eventRepository.save(event), initiator);
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " c ID = " + eventId + ", не найдено."));

        if (updateEventAdminRequest.getEventDate() != null) {
            if ((event.getPublishedOn() != null) && updateEventAdminRequest.getEventDate().isAfter(event.getPublishedOn().minusHours(1))) {
                throw new ConditionNotMetException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }
        if (updateEventAdminRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
            throw new ConditionNotMetException("Cобытие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        if (updateEventAdminRequest.getStateAction() == AdminStateAction.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
            throw new ConditionNotMetException("Cобытие можно отклонить, только если оно еще не опубликовано");
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
                event.setState(EventState.CANCELED);
            }
        }

        checkEvent(event, updateEventAdminRequest);
        return eventMapper.toEventFullDto(eventRepository.save(event), findShortUser(event.getInitiator()));

    }

    @Override
    public List<EventFullDto> getAllEventByInitiatorId(Long initiatorId) {
        UserShortDto initiator = userClient.getUserShortById(initiatorId);
        return eventRepository.findAllByInitiator(initiatorId).stream()
                .map(event -> eventMapper.toEventFullDto(event, initiator))
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " c ID = " + eventId + ", не найдено."));
        return eventMapper.toEventFullDto(event, findShortUser(event.getInitiator()));
    }

    @Override
    public List<EventShortDto> getRecommendationEvents(Long userId, Integer maxResult) {
        List<Long> eventIds = statClient.getRecommendationsForUser(userId, maxResult)
                .map(RecommendedEventProto::getEventId)
                .toList();
        List<Event> events = eventRepository.findAllByIdIsIn(eventIds);
        Map<Long, UserShortDto> usersMap = getUsersMap(events.stream().map(Event::getInitiator).toList());
        Map<Long, Double> ratingMap = getRating(eventIds);
        List<EventShortDto> eventShortDtos = events.stream()
                .map(e -> eventMapper.mapToShortDto(e, ratingMap.getOrDefault(e.getId(), 0.0), usersMap.get(e.getInitiator())))
                .toList();
        Map<Long, Long> requestsMap = getRequests(eventShortDtos.stream().map(EventShortDto::getId).toList());
        for (EventShortDto eventShortDto : eventShortDtos) {
            eventShortDto.setConfirmedRequests(requestsMap.getOrDefault(eventShortDto.getId(), 0L));
        }
        return eventShortDtos;
    }

    @Override
    public EventShortDto likeEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " c ID = " + eventId + ", не найдено."));
        List<ParticipationRequestDto> requestDtos = requestsClient.findAllByEventIdIn(userId, Collections.singletonList(eventId));
        boolean isParticipant = requestDtos.stream().anyMatch(request -> request.getRequester().equals(userId));
        if (!isParticipant) {
            throw new ValidationException(Event.class, " Пользователь с ID - " + userId + ", не участвует в событии с ID - " + eventId + ".");
        }
        statClient.collectUserAction(userId, eventId, UserActionType.LIKE);
        Map<Long, Double> ratingMap = getRating(Collections.singletonList(eventId));
        EventShortDto eventShortDto = eventMapper.mapToShortDto(event, ratingMap.getOrDefault(event.getId(), 0.0), findShortUser(event.getInitiator()));
        eventShortDto.setConfirmedRequests(getRequests(eventId));
        return eventShortDto;
    }

    @Override
    public List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size) {
        UserShortDto initiator = userClient.getUserShortById(userId);
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByInitiator(userId, pageable);
        List<EventFullDto> eventFullDtos = events.stream()
                .map(event -> eventMapper.toEventFullDto(event, initiator))
                .toList();
        Map<Long, Double> ratingMap = getRating(eventFullDtos.stream().map(EventFullDto::getId).toList());
        Map<Long, Long> requestsMap = getRequests(eventFullDtos.stream().map(EventFullDto::getId).toList());
        for (EventFullDto eventFullDto : eventFullDtos) {
            eventFullDto.setRating(ratingMap.getOrDefault(eventFullDto.getId(), 0.0));
            eventFullDto.setConfirmedRequests(requestsMap.getOrDefault(eventFullDto.getId(), 0L));
        }
        return eventMapper.toEventShortDtos(eventFullDtos);
    }

    @Override
    public EventFullDto findUserEventById(Long userId, Long eventId) {
        UserShortDto initiator = userClient.getUserShortById(userId);
        Event event = eventRepository.findByIdAndInitiator(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " Событие не найдено"));

        EventFullDto result = eventMapper.toEventFullDto(event, initiator);
        result.setRating(getRating(List.of(result.getId())).getOrDefault(result.getId(), 0.0));
        result.setConfirmedRequests(getRequests(result.getId()));
        return result;
    }

    @Override
    public EventFullDto getEventByIdAndInitiatorId(Long userId, Long eventId) {
        UserShortDto initiator = userClient.getUserShortById(userId);
        Event event = eventRepository.findByIdAndInitiator(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " Событие не найдено c ID - event ID" + eventId + " и initiator ID - " + userId));
        return eventMapper.toEventFullDto(event, initiator);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        UserShortDto initiator = userClient.getUserShortById(userId);
        Event event = eventRepository.findByIdAndInitiator(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие не найдено"));
        if (event.getState() == EventState.PUBLISHED) {
            throw new InitiatorRequestException("Нельзя отредактировать опубликованное событие");
        }

        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(NewEventDto.class, "До начала события осталось меньше двух часов");
            }
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == PrivateStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
            if (updateRequest.getStateAction() == PrivateStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            }
        }

        checkEvent(event, updateRequest);
        return eventMapper.toEventFullDto(eventRepository.save(event), findShortUser(userId));
    }

    private void checkEvent(Event event, UpdateEventBaseRequest updateRequest) {
        if (updateRequest.getAnnotation() != null && !updateRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException(Category.class, "Категория не найдена"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null && !updateRequest.getDescription().isBlank()) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            Optional<Location> locationOpt = locationRepository.findByLatAndLon(
                    updateRequest.getLocation().getLat(),
                    updateRequest.getLocation().getLon());
            Location location = locationOpt.orElse(locationRepository.save(
                    new Location(null, updateRequest.getLocation().getLat(), updateRequest.getLocation().getLon())));
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit().longValue());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    private Map<Long, Long> getRequests(List<Long> eventIds) {
        List<ParticipationRequestDto> requests = requestsClient.findAllByEventIdIn(0L, eventIds);
        return requests.stream()
                .collect(Collectors.groupingBy(ParticipationRequestDto::getEvent, Collectors.counting()));
    }

    private Long getRequests(Long eventId) {
        List<ParticipationRequestDto> requestDtos = requestsClient.findAllByEventIdIn(0L, Collections.singletonList(eventId));
        Map<Long, Long> requestsMap = requestDtos.stream()
                .collect(Collectors.groupingBy(ParticipationRequestDto::getEvent, Collectors.counting()));
        return requestsMap.getOrDefault(eventId, 0L);
    }

    private UserShortDto findShortUser(Long userId) {
        return userClient.getUserShortById(userId);
    }

    private Map<Long, UserShortDto> getUsersMap(List<Long> userIds) {
        return userClient.getAllUsersShort(userIds).stream()
                .collect(Collectors.toMap(UserShortDto::getId, user -> user));
    }

    private Map<Long, Double> getRating(List<Long> events) {
        List<RecommendedEventProto> ratingList = statClient.getInteractionsCount(events).toList();
        return ratingList.stream()
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }
}