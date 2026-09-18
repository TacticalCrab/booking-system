package com.example.bookingsystem.outbox;

import com.example.bookingsystem.common.logging.CorrelationId;
import com.example.bookingsystem.config.RabbitConfig;
import com.example.bookingsystem.notification.BookingNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            RabbitTemplate rabbitTemplate
    ) {
        repository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean publishNext()
            throws ExecutionException, InterruptedException, TimeoutException {
        Optional<OutboxEvent> pending = repository.findNextPendingForUpdate();

        if (pending.isEmpty()) {
            return false;
        }

        OutboxEvent event = pending.get();

        String routingKey = switch(event.getEventType()) {
            case BOOKING_CREATED -> RabbitConfig.CONFIRMATION_QUEUE;
        };

        var message = MessageBuilder
                .withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(event.getId().toString())
                .setHeader(
                        CorrelationId.HEADER,
                        event.getCorrelationId()
                )
                .build();

        var correlation = new CorrelationData(UUID.randomUUID().toString());

        log.debug("Publishing outbox event: eventId={}", event.getId());
        rabbitTemplate.send("", routingKey, message, correlation);

        var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);

        if (!confirm.ack()) {
            throw new IllegalArgumentException(
                    "RabbitMQ rejected outbox event " + event.getId()
                        + ": " + confirm.reason()
            );
        }

        if (correlation.getReturned() != null) {
            throw new IllegalStateException(
                    "No queue accepted outbox event " + event.getId()
            );
        }

        event.markPublished();
        return true;
    }
}
