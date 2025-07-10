package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class WeightSum {
    private Long eventId;
    private Double weightSum;
}