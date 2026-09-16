package com.example.bookingsystem.notification;

import com.example.bookingsystem.booking.event.BookingCreatedEvent;
import com.example.bookingsystem.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
public class BookingNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(BookingNotificationListener.class);
    private final EmailService emailService;

    public BookingNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.CONFIRMATION_QUEUE)
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
