package com.example.bookingsystem.booking.dto;

import java.util.List;

public record BookingCursorResponse(
        List<BookingResponse> content,
        Long nextCursor,
        boolean hasNext
) {}