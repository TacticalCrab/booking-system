package com.example.bookingsystem.notification;

import com.example.bookingsystem.booking.event.BookingCreatedEvent;
import com.example.bookingsystem.common.logging.CorrelationId;
import com.example.bookingsystem.outbox.OutboxEvent;
import com.example.bookingsystem.outbox.OutboxEventRepository;
import com.example.bookingsystem.outbox.OutboxEventType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
public class BookingNotificationPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    public BookingNotificationPublisher(
            OutboxEventRepository outboxEventRepository,
            JsonMapper jsonMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.jsonMapper = jsonMapper;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishCreated(BookingCreatedEvent event) {
        String correlationId = CorrelationId.currentOrCreate();

        String payload = jsonMapper.writeValueAsString(event);

        outboxEventRepository.save(new OutboxEvent(
                OutboxEventType.BOOKING_CREATED,
                payload,
                correlationId
        ));
    }
}
