package com.example.bookingsystem.booking.exception;

public class BookingConflictException extends RuntimeException {
    public BookingConflictException() {
        super("Requested booking time is unavailable");
    }
}
