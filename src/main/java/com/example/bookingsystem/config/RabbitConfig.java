package com.example.bookingsystem.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;


@Configuration
public class RabbitConfig {

    public static final String CONFIRMATION_QUEUE = "booking.confirmation";
    public static final String FAILED_QUEUE = "booking.confirmation.failed";

    @Bean
    public Queue confirmationQueue() {
        return QueueBuilder.durable(CONFIRMATION_QUEUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(FAILED_QUEUE)
                .build();
    }

    @Bean
    public Queue failedConfirmationQueue() {
        return QueueBuilder
                .durable(FAILED_QUEUE)
                .build();
    }

    @Bean
    public JacksonJsonMessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
