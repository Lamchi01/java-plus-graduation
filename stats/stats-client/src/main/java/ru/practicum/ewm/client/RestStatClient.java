package ru.practicum.ewm.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.protobuf.*;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class RestStatClient implements StatClient {
    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsClient;

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub userActionClient;

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto requestProto = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResult(maxResults)
                .build();
        Iterator<RecommendedEventProto> iterator = recommendationsClient.getSimilarEvents(requestProto);
        return asStream(iterator);
    }

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto requestProto = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResult(maxResults)
                .build();
        Iterator<RecommendedEventProto> iterator = recommendationsClient.getRecommendationsForUser(requestProto);
        return asStream(iterator);
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIdList) {
        InteractionsCountRequestProto requestProto = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIdList)
                .build();
        Iterator<RecommendedEventProto> iterator = recommendationsClient.getInteractionsCount(requestProto);
        return asStream(iterator);
    }

    @Override
    public void collectUserAction(long userId, long eventId, UserActionType action) {
        ActionTypeProto actionTypeProto = switch (action) {
            case LIKE -> ActionTypeProto.ACTION_LIKE;
            case VIEW -> ActionTypeProto.ACTION_VIEW;
            case REGISTER -> ActionTypeProto.ACTION_REGISTER;
        };
        UserActionProto userActionProto = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionTypeProto)
                .setTimestamp(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build())
                .build();
        userActionClient.collectUserAction(userActionProto);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}