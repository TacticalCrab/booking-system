package com.example.bookingsystem;

import com.example.bookingsystem.ratelimit.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RateLimitProperties.class)
@SpringBootApplication
public class BookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingSystemApplication.class, args);
    }

}
