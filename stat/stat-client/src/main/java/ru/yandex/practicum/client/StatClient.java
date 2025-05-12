package ru.yandex.practicum.client;

import ru.yandex.practicum.ParamDto;
import ru.yandex.practicum.ParamHitDto;
import ru.yandex.practicum.ViewStats;

import java.util.List;

public interface StatClient {
    void hit(ParamHitDto paramHitDto);

    List<ViewStats> getStat(ParamDto paramDto);
}