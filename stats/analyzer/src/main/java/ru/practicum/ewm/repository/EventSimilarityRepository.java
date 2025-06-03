package ru.practicum.ewm.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.EventSimilarity;
import ru.practicum.ewm.model.EventSimilarityId;

import java.util.List;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {
    List<EventSimilarity> findAllByEventAOrEventB(Long eventA, Long eventB, Sort sort);

    List<EventSimilarity> findAllByEventAInOrEventBIn(List<Long> eventAIds, List<Long> eventBIds, Sort sort);
}