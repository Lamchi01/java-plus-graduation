package ru.practicum.ewm.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.WeightSum;
import ru.practicum.ewm.model.EventSimilarity;
import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.repository.EventSimilarityRepository;
import ru.practicum.ewm.repository.UserActionRepository;
import ru.practicum.ewm.stats.protobuf.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.protobuf.RecommendedEventProto;
import ru.practicum.ewm.stats.protobuf.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.protobuf.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationsHandlerImpl implements RecommendationsHandler {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;
    private static final int MAX_LAST_VISITED_EVENTS_COUNT = 20;
    private static final int MAX_SIMILAR_NEIGHBOURS_COUNT = 3;

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto userPredictionsRequestProto) {
        Sort sort = Sort.by("lastActionDate").descending();
        Pageable pageable = PageRequest.of(0, MAX_LAST_VISITED_EVENTS_COUNT, sort);
        Long userId = userPredictionsRequestProto.getUserId();
        List<Long> recentVisits = userActionRepository.findAllByUserId(userId, pageable).stream()
                .map(UserAction::getEventId)
                .toList();

        List<EventSimilarity> neighbours = eventSimilarityRepository.findAllByEventAInOrEventBIn(
                recentVisits,
                recentVisits,
                sort
        );
        List<Long> unvisited = getUnvisitedEvents(neighbours, userId);
        List<Long> recommendedEvents = new ArrayList<>();
        for (EventSimilarity eventSimilarity : neighbours) {
            if (recommendedEvents.size() == userPredictionsRequestProto.getMaxResult()) break;
            Long eventA = eventSimilarity.getEventA();
            if (unvisited.contains(eventA)) {
                recommendedEvents.add(eventA);
                continue;
            }
            Long eventB = eventSimilarity.getEventB();
            if (unvisited.contains(eventB)) {
                recommendedEvents.add(eventB);
                continue;
            }
        }

        neighbours = eventSimilarityRepository.findAllByEventAInOrEventBIn(
                recommendedEvents,
                recommendedEvents,
                Sort.by("score").descending()
        );

        Map<Long, Double> userActionsMap = getUserActionsForEvents(neighbours, userId).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight));
        Map<Long, Map<Long, EventSimilarity>> eventSimilarityMapForCalculate = new HashMap<>();
        int maxCount = recommendedEvents.size() * MAX_SIMILAR_NEIGHBOURS_COUNT;
        int count = 0;
        for (EventSimilarity eventSimilarity : neighbours) {
            if (count == maxCount) break;
            Long eventA = eventSimilarity.getEventA();
            Long eventB = eventSimilarity.getEventB();
            Long eventForCalculate = null;
            Map<Long, EventSimilarity> eventSimilarityMap = null;

            if (userActionsMap.containsKey(eventA)) {
                eventSimilarityMap = eventSimilarityMapForCalculate.computeIfAbsent(eventB, k -> new HashMap<>());
                eventForCalculate = eventA;
            }

            if (userActionsMap.containsKey(eventB)) {
                eventSimilarityMap = eventSimilarityMapForCalculate.computeIfAbsent(eventA, k -> new HashMap<>());
                eventForCalculate = eventB;
            }

            if (eventSimilarityMap != null && eventSimilarityMap.size() != MAX_SIMILAR_NEIGHBOURS_COUNT) {
                eventSimilarityMap.put(eventForCalculate, eventSimilarity);
                count++;
            }
        }
        Map<Long, Double> predictedScore = getPredictedScore(eventSimilarityMapForCalculate, userActionsMap);
        return predictedScore.entrySet().stream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build()).sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto similarEventsRequestProto) {
        Sort sort = Sort.by("score").descending();
        Long eventId = similarEventsRequestProto.getEventId();
        List<EventSimilarity> eventSimilarities = eventSimilarityRepository.findAllByEventAOrEventB(eventId, eventId, sort);
        List<Long> unvisitedEventIds = getUnvisitedEvents(eventSimilarities, similarEventsRequestProto.getUserId());
        return (eventSimilarities.stream()
                .filter(eventSimilarity -> unvisitedEventIds.contains(eventSimilarity.getEventA())
                        || unvisitedEventIds.contains(eventSimilarity.getEventB()))
                .map(eventSimilarity -> RecommendedEventProto.newBuilder()
                        .setEventId(eventSimilarity.getEventA().equals(eventId) ? eventSimilarity.getEventB() : eventSimilarity.getEventA())
                        .setScore(eventSimilarity.getScore())
                        .build())
        ).limit(similarEventsRequestProto.getMaxResult()).toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto interactionsCountRequestProto) {
        List<WeightSum> weightSums = userActionRepository.getWeightSumByEventIdIn(interactionsCountRequestProto.getEventIdList());
        return weightSums.stream()
                .map(weightSum -> RecommendedEventProto.newBuilder()
                        .setEventId(weightSum.getEventId())
                        .setScore(weightSum.getWeightSum())
                        .build())
                .toList();
    }

    private List<Long> getUnvisitedEvents(List<EventSimilarity> allEvents, Long userId) {
        List<Long> allEventIds = allEvents.stream()
                .flatMap(eventSimilarity -> Stream.of(eventSimilarity.getEventA(), eventSimilarity.getEventB()))
                .toList();

        List<Long> visitedEventsIds = userActionRepository.findAllByUserIdAndEventIdIn(userId, allEventIds).stream()
                .map(UserAction::getEventId)
                .toList();

        return allEventIds.stream()
                .filter(eventId -> !visitedEventsIds.contains(eventId))
                .toList();
    }

    private List<UserAction> getUserActionsForEvents(List<EventSimilarity> allEvents, Long userId) {
        List<Long> allEventIds = allEvents.stream()
                .flatMap(eventSimilarity -> Stream.of(eventSimilarity.getEventA(), eventSimilarity.getEventB()))
                .toList();
        return userActionRepository.findAllByUserIdAndEventIdIn(userId, allEventIds);
    }

    private Map<Long, Double> getPredictedScore(Map<Long, Map<Long, EventSimilarity>> eventSimilarityMapForCalculate,
                                                Map<Long, Double> userActionsMap) {
        Map<Long, Double> predictedScore = new HashMap<>();
        for (Map.Entry<Long, Map<Long, EventSimilarity>> entry : eventSimilarityMapForCalculate.entrySet()) {
            double weightScoreSum = 0.0;
            double scoreSum = 0.0;
            for (Map.Entry<Long, EventSimilarity> similarityEntry : entry.getValue().entrySet()) {
                Double weight = userActionsMap.get(similarityEntry.getKey());
                Double score = similarityEntry.getValue().getScore();
                weightScoreSum += weight * score;
                scoreSum += weight;
            }
            predictedScore.put(entry.getKey(), weightScoreSum / scoreSum);
        }
        return predictedScore;
    }
}