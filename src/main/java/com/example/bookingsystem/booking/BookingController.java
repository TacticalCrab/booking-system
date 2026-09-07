package com.example.bookingsystem.booking;

import com.example.bookingsystem.auth.annotation.AdminOnly;
import com.example.bookingsystem.booking.dto.BookingResponse;
import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import com.example.bookingsystem.booking.dto.UpdateBookingStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/bookings")
class BookingController {
    private final BookingService service;

    public BookingController(
            BookingService bookingService
    ) {
        service = bookingService;
    }

    @GetMapping("/me")
    public Page<BookingResponse> getMine(
            @AuthenticationPrincipal UserDetails principal,
            Pageable pageable
    ) {
        return service.getByUserEmail(
                principal.getUsername(),
                pageable
        );
    }

    @GetMapping
    @AdminOnly
    public Page<BookingResponse> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public BookingResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return service.getById(id, principal.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return service.create(principal.getUsername(), request);
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return service.cancel(id, principal.getUsername());
    }

    @PatchMapping("/{id}/status")
    @AdminOnly
    public BookingResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        return service.updateStatus(id, request.status());
    }

}
