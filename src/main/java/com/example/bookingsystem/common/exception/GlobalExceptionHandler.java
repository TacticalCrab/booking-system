package com.example.bookingsystem.common.exception;

import com.example.bookingsystem.auth.exception.AccessDeniedException;
import com.example.bookingsystem.auth.exception.AuthenticationFailedException;
import com.example.bookingsystem.booking.exception.BookingConflictException;
import com.example.bookingsystem.booking.exception.InvalidBookingException;
import com.example.bookingsystem.employee.exception.InvalidEmployeeServiceException;
import com.example.bookingsystem.user.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiError> handleAuthenticationFailed(
            AuthenticationFailedException exception
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException exception
    ) {
        return buildError(HttpStatus.FORBIDDEN, exception);
    }

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

    @ExceptionHandler(InvalidEmployeeServiceException.class)
    public ResponseEntity<ApiError> handleInvalidEmployeeService(
            InvalidEmployeeServiceException exception
    ) {
        return buildError(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ApiError> handleInvalidBooking(
            InvalidBookingException exception
    ) {
        return buildError(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ApiError> handleBookingConflict(
            BookingConflictException exception
    ) {
        return buildError(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(
            UserAlreadyExistsException exception
    ) {
        return buildError(HttpStatus.CONFLICT, exception);
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
