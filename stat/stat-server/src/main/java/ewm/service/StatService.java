package ewm.service;

import ewm.ParamDto;
import ewm.ParamHitDto;
import ewm.ViewStats;

import java.util.List;

public interface StatService {
    void hit(ParamHitDto paramHitDto);

    List<ViewStats> getStat(ParamDto paramDto);
}