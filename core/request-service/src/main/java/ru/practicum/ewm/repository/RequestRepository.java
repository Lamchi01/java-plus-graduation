package ru.practicum.ewm.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.requests.model.Request;
import ru.practicum.ewm.requests.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByRequesterAndEvent(Long requestId, Long userId);

    @Query("SELECT COUNT (r) FROM Request r WHERE r.event = :eventId AND r.status = :status")
    Long countRequestsByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByRequester(Long userId);

    Optional<Request> findByIdAndRequester(Long requestId, Long userId);

    Optional<Request> findByEvent(Long eventId);

    Optional<Request> findByIdAndEvent(Long requestId, Long eventId);

    List<Request> findAllByEventInAndStatus(List<Long> eventIds, RequestStatus state);
}