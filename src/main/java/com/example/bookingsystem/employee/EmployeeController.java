package com.example.bookingsystem.employee;

import com.example.bookingsystem.auth.annotation.AdminOnly;
import com.example.bookingsystem.employee.dto.*;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(
            EmployeeService employeeService
    ) {
        service = employeeService;
    }

    @GetMapping
    public Page<EmployeeResponse> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public EmployeeResponse getAllById(
            @PathVariable Long id
    ) {
        return service.getById(id);
    }

    @GetMapping("/{id}/working-hours")
    public List<WorkingHoursResponse> getWorkingHours(
            @PathVariable Long id
    ) {
        return service.getWorkingHoursByEmployeeId(id);
    }

    @GetMapping("/{id}/availability")
    public EmployeeAvailabilityResponse getAvailability(
            @PathVariable Long id,
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.getAvailability(id, serviceId, date);
    }

    @PostMapping
    @AdminOnly
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @AdminOnly
    public EmployeeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return service.update(id, request);
    }

    @PutMapping("/{id}/working-hours")
    @AdminOnly
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replaceWorkingHours(
            @PathVariable Long id,
            @Valid @RequestBody ReplaceEmployeeWorkingHoursRequest request
    ) {
        service.replaceWorkingHours(id, request.workingDays());
    }
}
