package com.example.bookingsystem.employee;

import com.example.bookingsystem.employee.dto.CreateEmployeeRequest;
import com.example.bookingsystem.employee.dto.EmployeeResponse;
import com.example.bookingsystem.employee.dto.UpdateEmployeeRequest;
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
    public List<EmployeeResponse> getAll() {
        return service.getAll();
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
            @RequestBody CreateEmployeeRequest request
            ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(
            @PathVariable Long id,
            @RequestBody UpdateEmployeeRequest request
    ) {
        return service.update(id, request);
    }
}
