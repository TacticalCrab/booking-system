package com.example.bookingsystem.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent>
    findTop20ByPublishedAtIsNullOrderByCreatedAtAscIdAsc();

    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE published_at IS NULL
            ORDER BY created_at, id
            LIMIT 1
            FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    Optional<OutboxEvent> findNextPendingForUpdate();
}
