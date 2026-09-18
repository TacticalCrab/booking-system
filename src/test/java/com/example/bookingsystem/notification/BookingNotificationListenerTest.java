package com.example.bookingsystem.notification;

import com.example.bookingsystem.booking.event.BookingCreatedEvent;
import com.example.bookingsystem.common.logging.CorrelationId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingNotificationListenerTest {

    @Mock
    private EmailService emailService;

    @Test
    void shouldDelegateBookingCreatedEventToEmailService() {
        BookingCreatedEvent event = new BookingCreatedEvent(
                42L, "customer@example.com", "Haircut", LocalDateTime.of(2030, 1, 2, 10, 30)
        );

        new BookingNotificationListener(emailService).onBookingCreated(
                event,
                CorrelationId.generate()
        );

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(
                org.mockito.ArgumentMatchers.eq("customer@example.com"),
                org.mockito.ArgumentMatchers.eq("Booking confirmation #42"),
                body.capture()
        );
        assertTrue(body.getValue().contains("Booking number: 42"));
        assertTrue(body.getValue().contains("Service: Haircut"));
        assertTrue(body.getValue().contains("Start Time: 2030-01-02T10:30"));
    }
}
