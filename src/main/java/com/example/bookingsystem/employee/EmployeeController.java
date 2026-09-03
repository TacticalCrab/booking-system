package com.example.bookingsystem.employee;

import com.example.bookingsystem.employee.dto.CreateEmployeeRequest;
import com.example.bookingsystem.employee.dto.EmployeeResponse;
import com.example.bookingsystem.employee.dto.UpdateEmployeeRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return service.update(id, request);
    }
}
