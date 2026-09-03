package com.example.bookingsystem.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record UpdateServiceRequest (
        @NotBlank
        String name,

        @NotBlank
        String description,

        @NotNull
        @PositiveOrZero
        Integer durationMinutes,

        @NotNull
        @PositiveOrZero
        BigDecimal price
) {}