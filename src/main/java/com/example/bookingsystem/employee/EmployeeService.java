package com.example.bookingsystem.employee;

import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.employee.dto.CreateEmployeeRequest;
import com.example.bookingsystem.employee.dto.EmployeeResponse;
import com.example.bookingsystem.employee.dto.UpdateEmployeeRequest;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class EmployeeService {

    private final EmployeeRepository repository;
    private final ServiceRepository serviceRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            ServiceRepository serviceRepository
    ) {
        repository = employeeRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<EmployeeResponse> getAll() {
        return repository
                .findAll()
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    public EmployeeResponse getById(Long id) {
        Employee employee = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Employee", id));

        return EmployeeMapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        List<ServiceEntity> services = serviceRepository
                .findAllById(request.servicesIds());

        Employee employee = new Employee(
                request.name(),
                request.email(),
                services
        );

        Employee savedEmployee = repository.save(employee);

        return EmployeeMapper.toResponse(savedEmployee);
    }

    @Transactional
    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
        Employee employee = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Employee", id));

        employee.setName(request.name());
        employee.setEmail(request.email());

        List<ServiceEntity> services = serviceRepository
                .findAllById(request.servicesIds());

        Set<Long> foundIds = services.stream()
                .map(ServiceEntity::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = new HashSet<>(request.servicesIds());
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Service", missingIds);
        }

        employee.setServices(services);

        Employee savedEmployee = repository.save(employee);

        return EmployeeMapper.toResponse(savedEmployee);
    }
}
