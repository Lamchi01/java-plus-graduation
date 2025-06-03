package ru.practicum.ewm.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.StatClient;
import ru.practicum.ewm.client.UserActionType;
import ru.practicum.ewm.controller.clients.EventClient;
import ru.practicum.ewm.controller.clients.UserClient;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.model.Request;
import ru.practicum.ewm.requests.model.RequestStatus;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    private final UserClient userClient;
    private final EventClient eventClient;
    private final StatClient statClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<Request> requests = requestRepository.findByRequester(userId);
        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        UserShortDto user = userClient.getUserShortById(userId);
        EventFullDto event = eventClient.getEventFullById(eventId);

        if (event.getInitiator().getId().equals(user.getId())) {
            throw new InitiatorRequestException("Инициатор не может оформить запрос в своем собственном событии.");
        }
        if (requestRepository.findByRequesterAndEvent(userId, eventId).isPresent()) {
            throw new RepeatUserRequestorException("Пользователь с ID - " + userId + ", уже заявился на событие с ID - " + eventId + ".");
        }
        if (!event.getState().equals(EventState.PUBLISHED.toString())) {
            throw new NotPublishEventException("Данное событие ещё не опубликовано");
        }

        Request request = new Request();
        request.setRequester(user.getId());
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
        statClient.collectUserAction(userId, eventId, UserActionType.REGISTER);
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
        List<EventFullDto> userEvents = eventClient.getAllEventByInitiatorId(userId);
        EventFullDto event = userEvents.stream()
                .filter(e -> e.getInitiator().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь с ID - " + userId + ", не является инициатором события с ID - " + eventId + "."));
        return requestRepository.findByEvent(event.getId()).stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventRequest) {
        EventFullDto event = eventClient.getEventByIdAndInitiatorId(userId, eventId);
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
    public List<ParticipationRequestDto> findAllByEventIdIn(List<Long> eventIds) {
        return requestRepository.findAllByEventInAndStatus(eventIds, RequestStatus.CONFIRMED).stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }
}