package com.example.bookingsystem.idempotency;

import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingRequestHasherTest {

    private final BookingRequestHasher hasher = new BookingRequestHasher();

    @Test
    void shouldProduceDeterministicSha256Hash() {
        CreateBookingRequest request = request(1L, 2L, LocalDateTime.of(2030, 1, 1, 10, 0));

        String first = hasher.hash(request);
        String second = hasher.hash(request);

        assertEquals(first, second);
        assertEquals(64, first.length());
        assertTrue(first.matches("[0-9a-f]{64}"));
    }

    @Test
    void shouldChangeHashWhenAnyRequestFieldChanges() {
        CreateBookingRequest baseline = request(1L, 2L, LocalDateTime.of(2030, 1, 1, 10, 0));
        String hash = hasher.hash(baseline);

        assertNotEquals(hash, hasher.hash(request(3L, 2L, baseline.startTime())));
        assertNotEquals(hash, hasher.hash(request(1L, 4L, baseline.startTime())));
        assertNotEquals(hash, hasher.hash(request(1L, 2L, baseline.startTime().plusMinutes(30))));
    }

    private CreateBookingRequest request(Long employeeId, Long serviceId, LocalDateTime startTime) {
        return new CreateBookingRequest(employeeId, serviceId, startTime);
    }
}
