package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.config.KafkaProducerProperties;
import ru.practicum.ewm.producer.KafkaProducerClient;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.protobuf.ActionTypeProto;
import ru.practicum.ewm.stats.protobuf.UserActionProto;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {
    private final KafkaProducerClient kafkaProducerClient;
    private final KafkaProducerProperties kafkaProducerProperties;

    @Override
    public void collectUserAction(UserActionProto request) {
        UserActionAvro userActionAvro = mapToAvro(request);
        kafkaProducerClient.send(
                kafkaProducerProperties.getTopic().getUserAction(),
                userActionAvro.getTimestamp(),
                userActionAvro.getEventId(),
                userActionAvro
        );
    }

    private UserActionAvro mapToAvro(UserActionProto userActionProto) {
        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setTimestamp(Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(), userActionProto.getTimestamp().getNanos()))
                .setActionType(mapToActionTypeAvro(userActionProto.getActionType()))
                .build();
    }

    private ActionTypeAvro mapToActionTypeAvro(ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;

            case UNRECOGNIZED -> throw new IllegalArgumentException("Неизвестное значение ActionTypeProto: " +
                    ActionTypeProto.class.getSimpleName());
        };
    }
}