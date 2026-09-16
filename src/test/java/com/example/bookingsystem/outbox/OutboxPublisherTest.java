package com.example.bookingsystem.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldReturnFalseWhenThereIsNoPendingEvent() throws Exception {
        when(repository.findNextPendingForUpdate()).thenReturn(Optional.empty());

        assertFalse(publisher().publishNext());
    }

    @Test
    void shouldMarkEventPublishedAfterRabbitConfirmsIt() throws Exception {
        OutboxEvent event = event();
        when(repository.findNextPendingForUpdate()).thenReturn(Optional.of(event));
        confirm(true);

        assertTrue(publisher().publishNext());

        assertNotNull(event.getPublishedAt());
        ArgumentCaptor<org.springframework.amqp.core.Message> message =
                ArgumentCaptor.forClass(org.springframework.amqp.core.Message.class);
        verify(rabbitTemplate).send(
                org.mockito.ArgumentMatchers.eq(""),
                org.mockito.ArgumentMatchers.eq("booking.confirmation"),
                message.capture(),
                any(CorrelationData.class)
        );
        assertNotNull(message.getValue().getMessageProperties().getMessageId());
    }

    @Test
    void shouldNotMarkEventPublishedWhenRabbitNacksIt() throws Exception {
        OutboxEvent event = event();
        when(repository.findNextPendingForUpdate()).thenReturn(Optional.of(event));
        confirm(false);

        assertThrows(IllegalArgumentException.class, () -> publisher().publishNext());

        assertNull(event.getPublishedAt());
    }

    @Test
    void shouldNotMarkEventPublishedWhenRabbitReturnsIt() throws Exception {
        OutboxEvent event = event();
        when(repository.findNextPendingForUpdate()).thenReturn(Optional.of(event));
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            correlation.setReturned(new ReturnedMessage(
                    invocation.getArgument(2), 312, "NO_ROUTE", "", "booking.confirmation"
            ));
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).send(any(String.class), any(String.class), any(), any(CorrelationData.class));

        assertThrows(IllegalStateException.class, () -> publisher().publishNext());

        assertNull(event.getPublishedAt());
    }

    @Test
    void shouldPublishAnEventWhenItIsRetriedAfterANack() throws Exception {
        OutboxEvent event = event();
        when(repository.findNextPendingForUpdate()).thenReturn(Optional.of(event));
        AtomicInteger attempts = new AtomicInteger();
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            boolean accepted = attempts.incrementAndGet() == 2;
            correlation.getFuture().complete(new CorrelationData.Confirm(accepted, "rejected"));
            return null;
        }).when(rabbitTemplate).send(any(String.class), any(String.class), any(), any(CorrelationData.class));

        assertThrows(IllegalArgumentException.class, () -> publisher().publishNext());
        assertNull(event.getPublishedAt());
        assertTrue(publisher().publishNext());
        assertNotNull(event.getPublishedAt());
    }

    private OutboxPublisher publisher() {
        return new OutboxPublisher(repository, rabbitTemplate);
    }

    private OutboxEvent event() {
        OutboxEvent event = new OutboxEvent(
                OutboxEventType.BOOKING_CREATED, "{\"bookingId\":42}"
        );
        ReflectionTestUtils.setField(event, "id", 42L);
        return event;
    }

    private void confirm(boolean accepted) {
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(accepted, "rejected"));
            return null;
        }).when(rabbitTemplate).send(any(String.class), any(String.class), any(), any(CorrelationData.class));
    }
}
