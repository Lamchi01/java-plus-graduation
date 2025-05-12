package ewm.requests.repository;

import ewm.event.model.Event;
import ewm.requests.model.Request;
import ewm.requests.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByRequesterIdAndEventId(Long requestId, Long userId);

    @Query("SELECT COUNT (r) FROM Request r WHERE r.event = :event AND r.status = :status")
    Long countRequestsByEventAndStatus(Event event, RequestStatus status);

    List<Request> findByRequesterId(Long userId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    Optional<Request> findByEventId(Long eventId);

    Optional<Request> findByIdAndEventId(Long requestId, Long eventId);

    List<Request> findAllByEventIdInAndStatus(List<Long> eventIds, RequestStatus state);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);
}