package com.example.bookingsystem.service.dto;

import java.math.BigDecimal;

public record CreateServiceRequest(
    String name,
    String description,
    Integer durationMinutes,
    BigDecimal price
) { }
