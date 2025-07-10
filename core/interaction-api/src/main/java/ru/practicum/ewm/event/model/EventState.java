package ru.practicum.ewm.event.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static Set<EventState> from(List<String> states) {
        return states.stream()
                .map(state -> {
                    for (EventState value : EventState.values()) {
                        if (value.name().equalsIgnoreCase(state)) {
                            return value;
                        }
                    }
                    return null;
                }).collect(Collectors.toSet());
    }
}