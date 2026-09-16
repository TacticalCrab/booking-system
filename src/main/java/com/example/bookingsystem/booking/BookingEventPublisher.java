package com.example.bookingsystem.booking;

import com.example.bookingsystem.booking.event.BookingCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BookingEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public BookingEventPublisher(
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventPublisher = eventPublisher;
    }

    public void publishCreated(Booking booking) {
        eventPublisher.publishEvent(new BookingCreatedEvent(
                booking.getId(),
                booking.getUser().getEmail(),
                booking.getService().getName(),
                booking.getStartTime()
        ));
    }
}
