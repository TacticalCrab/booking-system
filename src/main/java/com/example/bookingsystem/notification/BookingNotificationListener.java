package com.example.bookingsystem.notification;

import com.example.bookingsystem.booking.event.BookingCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class BookingNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(BookingNotificationListener.class);
    private final EmailService emailService;

    public BookingNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onBookingCreated(BookingCreatedEvent event) {
        String body = """
                Your booking is confirmed!
                
                Booking number: %s
                Service: %s
                Start Time: %s
                """
                .formatted(
                    event.bookingId(),
                    event.serviceName(),
                    event.startTime()
        );

        emailService.send(
                event.customerEmail(),
                "Booking confirmation #" + event.bookingId(),
                body
        );
    }
}
