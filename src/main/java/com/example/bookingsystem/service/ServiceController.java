package com.example.bookingsystem.service;

import com.example.bookingsystem.service.dto.CreateServiceRequest;
import com.example.bookingsystem.service.dto.ServiceResponse;
import com.example.bookingsystem.service.dto.UpdateServiceRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
class ServiceController {

    private final ServiceService service;

    public ServiceController(
            ServiceService serviceService
    ) {
        service = serviceService;
    }

    @GetMapping
    public Page<ServiceResponse> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ServiceResponse getById(
            @PathVariable Long id
    ) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(
            @Valid @RequestBody CreateServiceRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ServiceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }
}
