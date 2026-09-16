package com.example.bookingsystem.idempotency;

import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class BookingRequestHasher {

    public String hash(CreateBookingRequest request) {
        String canonicalRequest = String.join(
                "|",
                String.valueOf(request.employeeId()),
                String.valueOf(request.serviceId()),
                String.valueOf(request.startTime())
        ).strip();

        return sha256(canonicalRequest);
    }


    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }
}
