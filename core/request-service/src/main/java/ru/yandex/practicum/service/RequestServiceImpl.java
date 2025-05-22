package ru.yandex.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.controller.clients.EventClient;
import ru.yandex.practicum.controller.clients.UserClient;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.EventState;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.mapper.RequestMapper;
import ru.yandex.practicum.repository.RequestRepository;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.requests.model.Request;
import ru.yandex.practicum.requests.model.RequestStatus;
import ru.yandex.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    private final UserClient userRepository;
    private final EventClient eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<Request> requests = requestRepository.findByRequester(userId);
        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (eventRepository.getEventByIdAndInitiatorId(eventId, userId).isPresent()) {
            throw new InitiatorRequestException("Пользователь с ID - " + userId + ", не найден.");
        }

        if (requestRepository.findByRequesterAndEvent(userId, eventId).isPresent()) {
            throw new RepeatUserRequestorException("Пользователь с ID - " + userId + ", уже заявился на событие с ID - " + eventId + ".");
        }
        Event event = eventRepository.getEventFullById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие с ID - " + eventId + ", не найдено."));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotPublishEventException("Данное событие ещё не опубликовано");
        }

        Request request = new Request();
        request.setRequester(userRepository.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь с ID - " + userId + ", не найден.")).getId());
        request.setEvent(event.getId());

        Long confirmedRequests = requestRepository.countRequestsByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        if (confirmedRequests >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new ParticipantLimitException("Достигнут лимит участников для данного события.");
        }

        request.setCreatedOn(LocalDateTime.now());
        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            return requestMapper.toParticipationRequestDto(requestRepository.save(request));
        }

        if (event.getRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
            return requestMapper.toParticipationRequestDto(requestRepository.save(request));
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request cancelRequest = requestRepository.findByIdAndRequester(requestId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Request.class, "Запрос с ID - " + requestId + ", не найден."));
        cancelRequest.setStatus(RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(requestRepository.save(cancelRequest));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        List<Event> userEvents = eventRepository.getAllEventByInitiatorId(userId);
        Event event = userEvents.stream()
                .filter(e -> e.getInitiator().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь с ID - " + userId + ", не является инициатором события с ID - " + eventId + "."));
        return requestRepository.findByEvent(event.getId()).stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventRequest) {
        Event event = eventRepository.getEventByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие с ID - " + eventId + ", не найдено."));
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new OperationUnnecessaryException("Запрос составлен некорректно.");
        }

        List<Long> requestIds = eventRequest.getRequestIds();
        List<Request> requests = requestIds.stream()
                .map(r -> requestRepository.findByIdAndEvent(r, eventId)
                        .orElseThrow(() -> new ValidationException("Запрос с ID - " + r + ", не найден.")))
                .toList();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        Long confirmedRequestsCount = requestRepository.countRequestsByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        if (confirmedRequestsCount >= event.getParticipantLimit()) {
            throw new ParticipantLimitException("Достигнут лимит участников для данного события.");
        }

        List<Request> updatedRequests = new ArrayList<>();

        for (Request request : requests) {
            if (request.getStatus().equals(RequestStatus.PENDING)) {
                if (eventRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
                    if (confirmedRequestsCount <= event.getParticipantLimit()) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        updatedRequests.add(request);
                        confirmedRequestsCount++;
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        updatedRequests.add(request);
                    }
                } else {
                    request.setStatus(eventRequest.getStatus());
                    updatedRequests.add(request);
                }
            }
        }

        List<Request> savedRequests = requestRepository.saveAll(updatedRequests);
        for (Request request : savedRequests) {
            if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
            } else {
                rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
            }
        }

        EventRequestStatusUpdateResult resultRequest = new EventRequestStatusUpdateResult();
        resultRequest.setConfirmedRequests(confirmedRequests);
        resultRequest.setRejectedRequests(rejectedRequests);
        return resultRequest;
    }

    @Override
    public Long getCountConfirmedRequestsByEventId(Long eventId) {
        return requestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public List<Request> findAllByEventIdIn(List<Long> eventIds) {
        return requestRepository.findAllByEventInAndStatus(eventIds, RequestStatus.CONFIRMED);
    }
}