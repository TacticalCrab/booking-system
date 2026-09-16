package com.example.bookingsystem.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxWorker {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxPublisher publisher;

    public OutboxWorker(OutboxPublisher publisher) {
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-delay-ms}")
    public void publishPending() {
        try {
            for (int i = 0; i < 20; i++) {
                if (!publisher.publishNext()) {
                    break;
                }
            }
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            log.warn(
                    "Outbox publishing failed; pending events will be retried",
                    exception
            );
        }
    }
}
