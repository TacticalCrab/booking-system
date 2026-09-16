package com.example.bookingsystem.idempotency.exception;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException() {
        super("Idempotency key was already used for a different request");
    }
}
