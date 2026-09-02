package com.example.bookingsystem.employee.dto;

import com.example.bookingsystem.service.dto.ServiceResponse;

import java.util.List;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        List<ServiceResponse> services
) { }
