package ru.practicum.ewm.service.handler;

import ru.practicum.ewm.stats.protobuf.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.protobuf.RecommendedEventProto;
import ru.practicum.ewm.stats.protobuf.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.protobuf.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationsHandler {
    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto userPredictionsRequestProto);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto similarEventsRequestProto);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto interactionsCountRequestProto);
}