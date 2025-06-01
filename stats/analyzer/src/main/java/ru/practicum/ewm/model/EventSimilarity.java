package ru.practicum.ewm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_similarity")
@IdClass(EventSimilarityId.class)
public class EventSimilarity {
    @Id
    private Long eventA;

    @Id
    private Long eventB;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Instant actionDate;
}