package ru.practicum.ewm.client;

import ru.practicum.ewm.stats.protobuf.RecommendedEventProto;

import java.util.List;
import java.util.stream.Stream;

public interface StatClient {
    Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults);

    Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults);

    Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIdList);

    void collectUserAction(long userId, long eventId, UserActionType action);
}