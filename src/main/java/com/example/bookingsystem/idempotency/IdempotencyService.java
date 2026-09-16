package com.example.bookingsystem.idempotency;

import com.example.bookingsystem.idempotency.exception.IdempotencyConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public IdempotencyRecord reserve(
            Long userId,
            String idempotencyKey,
            String requestHash
    ) {
        repository.insertIfAbsent(
                userId,
                idempotencyKey,
                requestHash
        );

        IdempotencyRecord record = repository
                .findAndLock(userId, idempotencyKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Reserved idempotency record was not found"
                ));

        if (record.hasDifferentRequestHash(requestHash)) {
            throw new IdempotencyConflictException();
        }

        return record;
    }
}
