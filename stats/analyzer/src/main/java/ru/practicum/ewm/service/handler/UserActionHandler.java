package ru.practicum.ewm.service.handler;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionHandler {
    void handleUserAction(UserActionAvro userActionAvro);
}