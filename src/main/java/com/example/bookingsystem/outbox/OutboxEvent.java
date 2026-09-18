package com.example.bookingsystem.outbox;


import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private OutboxEventType eventType;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    protected OutboxEvent() {
    }

    public OutboxEvent(
            OutboxEventType eventType,
            String payload,
            String correlationId
    ) {
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.correlationId = correlationId;
    }

    public void markPublished() {
        this.publishedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public OutboxEventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
