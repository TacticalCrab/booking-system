package com.example.bookingsystem.employee;

import com.example.bookingsystem.employee.dto.EmployeeResponse;
import com.example.bookingsystem.service.ServiceMapper;
import com.example.bookingsystem.service.dto.ServiceResponse;

import java.util.List;

public final class EmployeeMapper {
    private EmployeeMapper() {}

    public static EmployeeResponse toResponse(Employee employee) {
        List<ServiceResponse> services = employee
                .getServices()
                .stream()
                .map(ServiceMapper::toResponse)
                .toList();

        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                services
        );
    }
}
