package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.dto.WeightSum;
import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.model.UserActionId;

import java.util.List;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, UserActionId> {
    List<UserAction> findAllByUserIdAndEventIdIn(Long userId, List<Long> eventIds);

    List<UserAction> findAllByUserId(Long userId, Pageable pageable);

    @Query("""
                SELECT new ru.practicum.ewm.dto.WeightSum(a.eventId, sum(a.weight))
                FROM UserAction a
                WHERE a.eventId IN :eventIds
                GROUP BY a.eventId
            """)
    List<WeightSum> getWeightSumByEventIdIn(List<Long> eventIds);
}