package ewm.requests.service;

import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.repository.EventRepository;
import ewm.exception.*;
import ewm.requests.dto.EventRequestStatusUpdateRequest;
import ewm.requests.dto.EventRequestStatusUpdateResult;
import ewm.requests.dto.ParticipationRequestDto;
import ewm.requests.mapper.RequestMapper;
import ewm.requests.model.Request;
import ewm.requests.model.RequestStatus;
import ewm.requests.repository.RequestRepository;
import ewm.user.repository.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<Request> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (eventRepository.findByIdAndInitiatorId(eventId, userId).isPresent()) {
            throw new InitiatorRequestException("Пользователь с ID - " + userId + ", не найден.");
        }

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new RepeatUserRequestorException("Пользователь с ID - " + userId + ", уже заявился на событие с ID - " + eventId + ".");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие с ID - " + eventId + ", не найдено."));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotPublishEventException("Данное событие ещё не опубликовано");
        }

        Request request = new Request();
        request.setRequester(userRepository.findById(userId).get());
        request.setEvent(event);

        Long confirmedRequests = requestRepository.countRequestsByEventAndStatus(event, RequestStatus.CONFIRMED);
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
        Request cancelRequest = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Request.class, "Запрос с ID - " + requestId + ", не найден."));
        cancelRequest.setStatus(RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(requestRepository.save(cancelRequest));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        List<Event> userEvents = eventRepository.findAllByInitiatorId(userId);
        Event event = userEvents.stream()
                .filter(e -> e.getInitiator().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь с ID - " + userId + ", не является инициатором события с ID - " + eventId + "."));
        return requestRepository.findByEventId(event.getId()).stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие с ID - " + eventId + ", не найдено."));
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new OperationUnnecessaryException("Запрос составлен некорректно.");
        }

        List<Long> requestIds = eventRequest.getRequestIds();
        List<Request> requests = requestIds.stream()
                .map(r -> requestRepository.findByIdAndEventId(r, eventId)
                        .orElseThrow(() -> new ValidationException("Запрос с ID - " + r + ", не найден.")))
                .toList();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        Long confirmedRequestsCount = requestRepository.countRequestsByEventAndStatus(event, RequestStatus.CONFIRMED);

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
}