package com.example.bookingsystem.idempotency;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idempotency_user_key",
                columnNames = { "user_id", "idempotency_key" }
        )
)
public class IdempotencyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyRecord() {}

    public IdempotencyRecord(
            Long userId,
            String idempotencyKey,
            String requestHash
    ) {
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = IdempotencyStatus.PROCESSING;
        this.createdAt = Instant.now();
    }

    public void complete(Long bookingId) {
        this.bookingId = bookingId;
        this.status = IdempotencyStatus.COMPLETED;
    }

    public boolean hasDifferentRequestHash(String requestHash) {
        return !this.requestHash.equals(requestHash);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
