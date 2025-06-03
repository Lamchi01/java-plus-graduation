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
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        List<Long> recentVisits = getRecentUserVisits(request.getUserId());
        List<EventSimilarity> neighbours = findSimilarEvents(recentVisits, Sort.by("score").descending());
        List<Long> unvisitedEvents = getUnvisitedEvents(neighbours, request.getUserId());
        List<Long> recommendedEvents = buildInitialRecommendations(neighbours, unvisitedEvents, request.getMaxResult());
        List<EventSimilarity> recommendationNeighbours = findSimilarEvents(recommendedEvents, Sort.by("score").descending());
        Map<Long, Double> userActionsMap = getUserActionsMap(recommendationNeighbours, request.getUserId());
        Map<Long, Map<Long, EventSimilarity>> similarityMap = buildSimilarityMap(recommendationNeighbours, userActionsMap);
        Map<Long, Double> predictedScores = calculatePredictedScores(similarityMap, userActionsMap);
        return convertToRecommendedProtoList(predictedScores);
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<EventSimilarity> similarities = findSimilarEvents(
                List.of(request.getEventId()),
                Sort.by("score").descending());

        List<Long> unvisitedEvents = getUnvisitedEvents(similarities, request.getUserId());

        return similarities.stream()
                .filter(similarity -> isUnvisited(similarity, unvisitedEvents, request.getEventId()))
                .map(similarity -> buildRecommendedProto(similarity, request.getEventId()))
                .limit(request.getMaxResult())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<WeightSum> weightSums = userActionRepository.getWeightSumByEventIdIn(request.getEventIdList());
        return weightSums.stream()
                .map(this::convertWeightSumToProto)
                .toList();
    }

    private List<Long> getRecentUserVisits(Long userId) {
        Pageable pageable = PageRequest.of(0, MAX_LAST_VISITED_EVENTS_COUNT, Sort.by("lastActionDate").descending());
        return userActionRepository.findAllByUserId(userId, pageable).stream()
                .map(UserAction::getEventId)
                .toList();
    }

    private List<EventSimilarity> findSimilarEvents(List<Long> eventIds, Sort sort) {
        return eventSimilarityRepository.findAllByEventAInOrEventBIn(eventIds, eventIds, sort);
    }

    private List<Long> getUnvisitedEvents(List<EventSimilarity> similarities, Long userId) {
        List<Long> allEventIds = extractAllEventIds(similarities);
        List<Long> visitedEventIds = getVisitedEventIds(allEventIds, userId);

        return allEventIds.stream()
                .filter(eventId -> !visitedEventIds.contains(eventId))
                .distinct()
                .toList();
    }

    private List<Long> extractAllEventIds(List<EventSimilarity> similarities) {
        return similarities.stream()
                .flatMap(s -> Stream.of(s.getEventA(), s.getEventB()))
                .toList();
    }

    private List<Long> getVisitedEventIds(List<Long> eventIds, Long userId) {
        return userActionRepository.findAllByUserIdAndEventIdIn(userId, eventIds).stream()
                .map(UserAction::getEventId)
                .toList();
    }

    private List<Long> buildInitialRecommendations(List<EventSimilarity> neighbours,
                                                   List<Long> unvisitedEvents,
                                                   int maxResult) {
        Set<Long> recommendations = new LinkedHashSet<>();

        for (EventSimilarity similarity : neighbours) {
            if (recommendations.size() >= maxResult) break;

            Long eventA = similarity.getEventA();
            Long eventB = similarity.getEventB();

            if (unvisitedEvents.contains(eventA)) recommendations.add(eventA);
            if (recommendations.size() < maxResult && unvisitedEvents.contains(eventB)) {
                recommendations.add(eventB);
            }
        }

        return new ArrayList<>(recommendations);
    }

    private Map<Long, Double> getUserActionsMap(List<EventSimilarity> similarities, Long userId) {
        return userActionRepository.findAllByUserIdAndEventIdIn(
                        userId,
                        extractAllEventIds(similarities))
                .stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight));
    }

    private Map<Long, Map<Long, EventSimilarity>> buildSimilarityMap(
            List<EventSimilarity> neighbours,
            Map<Long, Double> userActions) {
        Map<Long, Map<Long, EventSimilarity>> similarityMap = new HashMap<>();
        int count = 0;
        int maxCount = neighbours.size() * MAX_SIMILAR_NEIGHBOURS_COUNT;

        for (EventSimilarity similarity : neighbours) {
            if (count >= maxCount) break;

            processSimilarityPair(similarityMap, similarity, userActions, similarity.getEventA(), similarity.getEventB());
            processSimilarityPair(similarityMap, similarity, userActions, similarity.getEventB(), similarity.getEventA());
            count++;
        }

        return similarityMap;
    }

    private void processSimilarityPair(
            Map<Long, Map<Long, EventSimilarity>> similarityMap,
            EventSimilarity similarity,
            Map<Long, Double> userActions,
            Long mainEvent,
            Long relatedEvent) {
        if (userActions.containsKey(relatedEvent)) {
            Map<Long, EventSimilarity> innerMap = similarityMap
                    .computeIfAbsent(mainEvent, k -> new HashMap<>());
            if (innerMap.size() < MAX_SIMILAR_NEIGHBOURS_COUNT) {
                innerMap.put(relatedEvent, similarity);
            }
        }
    }

    private Map<Long, Double> calculatePredictedScores(
            Map<Long, Map<Long, EventSimilarity>> similarityMap,
            Map<Long, Double> userActions) {
        Map<Long, Double> scores = new HashMap<>();

        similarityMap.forEach((eventId, similarities) -> {
            double weightScoreSum = 0.0;
            double weightSum = 0.0;

            for (Map.Entry<Long, EventSimilarity> entry : similarities.entrySet()) {
                Double weight = userActions.get(entry.getKey());
                Double score = entry.getValue().getScore();
                weightScoreSum += weight * score;
                weightSum += weight;
            }

            if (weightSum > 0) {
                scores.put(eventId, weightScoreSum / weightSum);
            }
        });

        return scores;
    }

    private List<RecommendedEventProto> convertToRecommendedProtoList(Map<Long, Double> predictedScores) {
        return predictedScores.entrySet().stream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .toList();
    }

    private boolean isUnvisited(EventSimilarity similarity, List<Long> unvisitedEvents, Long sourceEventId) {
        Long eventA = similarity.getEventA();
        Long eventB = similarity.getEventB();
        return (eventA.equals(sourceEventId) && unvisitedEvents.contains(eventB)) ||
                (eventB.equals(sourceEventId) && unvisitedEvents.contains(eventA));
    }

    private RecommendedEventProto buildRecommendedProto(EventSimilarity similarity, Long sourceEventId) {
        Long recommendedEventId = similarity.getEventA().equals(sourceEventId) ?
                similarity.getEventB() : similarity.getEventA();
        return RecommendedEventProto.newBuilder()
                .setEventId(recommendedEventId)
                .setScore(similarity.getScore())
                .build();
    }

    private RecommendedEventProto convertWeightSumToProto(WeightSum weightSum) {
        return RecommendedEventProto.newBuilder()
                .setEventId(weightSum.getEventId())
                .setScore(weightSum.getWeightSum())
                .build();
    }
}