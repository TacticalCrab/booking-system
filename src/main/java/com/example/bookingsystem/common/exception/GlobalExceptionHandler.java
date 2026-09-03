package com.example.bookingsystem.common.exception;

import com.example.bookingsystem.booking.exception.InvalidBookingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            NotFoundException exception
    ) {
        return buildError(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(InvalidTimeException.class)
    public ResponseEntity<ApiError> handleInvalidTime(
            InvalidTimeException exception
    ) {
        return buildError(HttpStatus.BAD_REQUEST, exception);
    }


    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ApiError> handleInvalidBooking(
            InvalidBookingException exception
    ) {
        return buildError(HttpStatus.BAD_REQUEST, exception);
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            Exception exception
    ) {
        ApiError error = new ApiError(
                status.value(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
