package com.example.bookingsystem.service;

import com.example.bookingsystem.service.dto.ServiceResponse;

public final class ServiceMapper {
    private ServiceMapper() {}

    public static ServiceResponse toResponse(ServiceEntity service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice()
        );
    }
}
