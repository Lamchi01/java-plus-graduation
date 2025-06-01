package ru.practicum.ewm.service;

import ru.practicum.ewm.stats.protobuf.UserActionProto;

public interface UserActionService {
    void collectUserAction(UserActionProto request);
}