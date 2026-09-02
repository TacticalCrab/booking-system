package com.example.bookingsystem.employee.dto;

import java.util.List;

public record CreateEmployeeRequest(
        String name,
        String email,
        List<Long> servicesIds
) { }
