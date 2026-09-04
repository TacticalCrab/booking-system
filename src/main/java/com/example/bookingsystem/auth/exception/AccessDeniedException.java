package com.example.bookingsystem.auth.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
        super("Access Denied");
    }
}
