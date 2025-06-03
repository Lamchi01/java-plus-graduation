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
@Table(name = "users_actions")
@IdClass(UserActionId.class)
public class UserAction {
    @Id
    private Long userId;

    @Id
    private Long eventId;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Instant lastActionDate;
}