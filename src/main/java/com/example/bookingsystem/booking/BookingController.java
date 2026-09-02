package com.example.bookingsystem.booking;

import com.example.bookingsystem.booking.dto.BookingResponse;
import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
class BookingController {
    private final BookingService service;

    public BookingController(
            BookingService bookingService
    ) {
        service = bookingService;
    }

    @GetMapping
    public List<BookingResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public BookingResponse getAllById(
            @PathVariable Long id
    ) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
           @RequestBody CreateBookingRequest request
    ) {
        return service.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }
}
