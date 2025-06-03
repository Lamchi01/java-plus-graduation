package ru.practicum.ewm.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.EventSimilarity;
import ru.practicum.ewm.model.EventSimilarityId;
import ru.practicum.ewm.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityHandlerImpl implements EventSimilarityHandler {
    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    @Override
    public void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.debug("Обращение eventSimilarityAvro: {}", eventSimilarityAvro);
        Optional<EventSimilarity> eventSimilarityOptional = eventSimilarityRepository.findById(EventSimilarityId.builder()
                .eventA(eventSimilarityAvro.getEventA())
                .eventB(eventSimilarityAvro.getEventB())
                .build());

        if (eventSimilarityOptional.isPresent()) {
            EventSimilarity eventSimilarity = eventSimilarityOptional.get();
            Instant timestamp = eventSimilarityAvro.getTimestamp();
            if (!eventSimilarity.getActionDate().isBefore(timestamp)) {
                log.debug("Неактуальное время события");
                return;
            }
            eventSimilarity.setScore(eventSimilarityAvro.getScore());
            eventSimilarity.setActionDate(timestamp);
            return;
        }
        log.debug("Добавление нового события, так как его нет в базе");
        eventSimilarityRepository.save(EventSimilarity.builder()
                .eventA(eventSimilarityAvro.getEventA())
                .eventB(eventSimilarityAvro.getEventB())
                .score(eventSimilarityAvro.getScore())
                .actionDate(eventSimilarityAvro.getTimestamp())
                .build());
    }
}