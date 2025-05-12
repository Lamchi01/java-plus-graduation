package ru.yandex.practicum.requests.model;

import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@ToString
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @Column(name = "created_at")
    private LocalDateTime createdOn;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}