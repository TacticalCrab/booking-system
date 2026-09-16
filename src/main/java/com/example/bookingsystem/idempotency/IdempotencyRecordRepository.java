package com.example.bookingsystem.idempotency;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, Long> {

    @Modifying
    @Query(
            value = """
            INSERT INTO idempotency_records (
                user_id,
                idempotency_key,
                request_hash,
                status,
                created_at
            )
            VALUES (
                :userId,
                :idempotencyKey,
                :requestHash,
                'PROCESSING',
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (user_id, idempotency_key)
            DO NOTHING
            """,
        nativeQuery = true
    )
    int insertIfAbsent(
            @Param("userId") Long userId,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("requestHash") String requestHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT record
        FROM IdempotencyRecord record
        WHERE record.userId = :userId
            AND record.idempotencyKey = :idempotencyKey
    """)
    Optional<IdempotencyRecord> findAndLock(
           @Param("userId") Long userId,
           @Param("idempotencyKey") String idempotencyKey
    );
}
