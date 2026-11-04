package com.ticketapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"}) // Aynı etkinliği 2 kere favorileyemesin
})
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private LocalDateTime createdAt;

    public Favorite() {}

    public Favorite(User user, Event event) {
        this.user = user;
        this.event = event;
        this.createdAt = LocalDateTime.now();
    }

    // Getterlar
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Event getEvent() { return event; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}