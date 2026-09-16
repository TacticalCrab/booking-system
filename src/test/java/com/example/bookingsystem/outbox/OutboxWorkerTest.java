package com.example.bookingsystem.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxWorkerTest {

    @Mock
    private OutboxPublisher publisher;

    @Test
    void shouldStopWhenThereAreNoPendingEvents() throws Exception {
        when(publisher.publishNext()).thenReturn(false);

        new OutboxWorker(publisher).publishPending();

        verify(publisher).publishNext();
    }

    @Test
    void shouldPublishAtMostTwentyEventsPerBatch() throws Exception {
        when(publisher.publishNext()).thenReturn(true);

        new OutboxWorker(publisher).publishPending();

        verify(publisher, times(20)).publishNext();
    }

    @Test
    void shouldLeaveFailedEventForTheNextRun() throws Exception {
        when(publisher.publishNext())
                .thenThrow(new IllegalStateException("publish failed"))
                .thenReturn(true)
                .thenReturn(false);

        OutboxWorker worker = new OutboxWorker(publisher);
        worker.publishPending();
        worker.publishPending();

        verify(publisher, times(3)).publishNext();
    }
}
