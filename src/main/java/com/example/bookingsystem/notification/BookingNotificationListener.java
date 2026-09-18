package com.example.bookingsystem.notification;

import com.example.bookingsystem.booking.event.BookingCreatedEvent;
import com.example.bookingsystem.common.logging.CorrelationId;
import com.example.bookingsystem.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;


@Component
public class BookingNotificationListener {

    private final Logger log = LoggerFactory.getLogger(BookingNotificationListener.class);
    private final EmailService emailService;

    public BookingNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.CONFIRMATION_QUEUE)
    public void onBookingCreated(
            BookingCreatedEvent event,
            @Header(
                    name = CorrelationId.HEADER,
                    required = false
            ) String correlationId
    ) {
        if (correlationId != null) {
            MDC.put(CorrelationId.MDC_KEY, correlationId);
        }

        try {
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

            log.info("Booking notification processed: bookingId={}", event.bookingId());
            emailService.send(
                    event.customerEmail(),
                    "Booking confirmation #" + event.bookingId(),
                    body
            );
        } finally {
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
