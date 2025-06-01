package ru.practicum.ewm.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.model.UserActionId;
import ru.practicum.ewm.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler {
    private final UserActionRepository userActionRepository;

    @Transactional
    @Override
    public void handleUserAction(UserActionAvro userActionAvro) {
        log.debug("Обращение userActionAvro: {}", userActionAvro);
        Optional<UserAction> userActionOptional = userActionRepository.findById(UserActionId.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .build());
        Double newWeight = getNewWeight(userActionAvro);
        Instant newTimestamp = userActionAvro.getTimestamp();
        if (userActionOptional.isPresent()) {
            UserAction userAction = userActionOptional.get();
            if (userAction.getWeight() < newWeight) {
                userAction.setWeight(newWeight);
            }
            if (userAction.getLastActionDate().isBefore(newTimestamp)) {
                userAction.setLastActionDate(newTimestamp);
            }
            return;
        }
        log.debug("Добавление нового действия пользователя, так как его нет в базе");
        userActionRepository.save(UserAction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .weight(newWeight)
                .lastActionDate(newTimestamp)
                .build());
    }

    private Double getNewWeight(UserActionAvro userActionAvro) {
        return switch (userActionAvro.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}