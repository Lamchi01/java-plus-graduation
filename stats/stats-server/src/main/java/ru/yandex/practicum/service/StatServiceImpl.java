package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.ParamDto;
import ru.yandex.practicum.ParamHitDto;
import ru.yandex.practicum.ViewStats;
import ru.yandex.practicum.exception.ValidateException;
import ru.yandex.practicum.mapper.StatMapper;
import ru.yandex.practicum.repository.StatRepository;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;

    @Transactional
    @Override
    public void hit(ParamHitDto paramHitDto) {
        statRepository.save(StatMapper.toEndpointHit(paramHitDto));
    }

    @Override
    public List<ViewStats> getStat(ParamDto paramDto) {
        if (!paramDto.getStart().isBefore(paramDto.getEnd())) {
            throw new ValidateException("Некорректный диапазон времени");
        }
        List<ViewStats> viewStatsList;
        if (paramDto.getUnique()) {
            viewStatsList = statRepository.findAllUniqueIpAndTimestampBetweenAndUriIn(
                    paramDto.getStart(),
                    paramDto.getEnd(),
                    paramDto.getUris());
        } else {
            viewStatsList = statRepository.findAllByTimestampBetweenAndUriIn(
                    paramDto.getStart(),
                    paramDto.getEnd(),
                    paramDto.getUris());
        }
        return viewStatsList;
    }
}