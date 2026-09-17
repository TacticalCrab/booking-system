package com.example.bookingsystem.ratelimit;

import com.example.bookingsystem.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "rate-limit.booking.capacity=3",
        "rate-limit.booking.refill-per-minute=3"
})
public class BookingRateLimitFilterIntegrationTest extends IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRateLimitBookingCreationPerUser() throws Exception {
        String invalidBody = "{}";

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(
                            post("/api/bookings")
                                    .with(jwt().jwt(jwt ->
                                            jwt.subject("1001")
                                    ))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidBody)
                    )
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(
                        post("/api/bookings")
                                .with(jwt().jwt(jwt ->
                                        jwt.subject("1001")
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidBody)
                )
                .andExpect(status().isTooManyRequests());


        mockMvc.perform(
                        post("/api/bookings")
                                .with(jwt().jwt(jwt ->
                                        jwt.subject("2002")
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidBody)
                )
                .andExpect(status().isBadRequest());
    }
}
