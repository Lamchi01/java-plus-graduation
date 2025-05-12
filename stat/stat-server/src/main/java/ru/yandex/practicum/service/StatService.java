package ru.yandex.practicum.service;

import ru.yandex.practicum.ParamDto;
import ru.yandex.practicum.ParamHitDto;
import ru.yandex.practicum.ViewStats;

import java.util.List;

public interface StatService {
    void hit(ParamHitDto paramHitDto);

    List<ViewStats> getStat(ParamDto paramDto);
}