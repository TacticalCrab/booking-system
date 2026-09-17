package com.example.bookingsystem.ratelimit;

import com.example.bookingsystem.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "rate-limit.login.capacity=5",
        "rate-limit.login.refill-per-minute=5"
})
public class RateLimitFilterIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRateLimitLoginAfterFiveRequests() throws Exception {
        String body = """
                {
                    "email": "does-not-exist@example.com",
                    "password": "wrong-password"
                }
                """;

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(
                    post("/api/auth/login")
                            .with(request -> {
                                request.setRemoteAddr("10.0.0.123");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
            ).andExpect(status().isUnauthorized());
        }

        mockMvc.perform(
                post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.123");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldHaveSeparateLimitsForDifferentIps() throws Exception {
       String body = """
                    {
                        "email": "does-not-exist@example.com",
                        "password": "wrong-password"
                    }
               """;

       for (int i = 0; i < 5; i++) {
           mockMvc.perform(
                   post("/api/auth/login")
                           .with(request -> {
                               request.setRemoteAddr("10.0.0.1");
                               return request;
                           })
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(body)
           )
           .andExpect(status().isUnauthorized());
       }

        mockMvc.perform(
                post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isTooManyRequests());

        mockMvc.perform(
                    post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.2");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotTrustXForwardedForFromClient() throws Exception {
        String body = """
                {
                    "email": "does-not-exist@example.com",
                    "password": "wrong-password"
                }
                """;

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(
                    post("/api/auth/login")
                            .with(request -> {
                                request.setRemoteAddr("10.0.0.100");
                                return request;
                            })
                            .header(
                                    "X-Forwarded-For",
                                    "1.2.3." + i
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
            )
                    .andExpect(status().isUnauthorized());
        }


        mockMvc.perform(
                        post("/api/auth/login")
                                .with(request -> {
                                    request.setRemoteAddr("10.0.0.100");
                                    return request;
                                })
                                .header(
                                        "X-Forwarded-For",
                                        "99.99.99.99"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isTooManyRequests());
    }

}
