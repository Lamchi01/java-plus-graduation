package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.dto.EventCommentCount;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    List<Comment> findAllByEventIdAndAuthor(Long eventId, Long authorId, Pageable pageable);

    List<Comment> findAllByAuthor(Long authorId, Pageable pageable);

    long countCommentByEvent_Id(Long eventId);

    @Query(value = """
            SELECT c.event_id AS eventId, count(event_id) AS commentCount
                   FROM comments c
                   WHERE c.event_id IN :eventsIds
                        GROUP BY c.event_id
            """, nativeQuery = true)
    List<EventCommentCount> findAllByEventIds(@Param("eventsIds") List<Long> eventsIds);
}