package com.example.bookingsystem.employee;

import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.employee.dto.*;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHoursMapper;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
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

    public Page<EmployeeResponse> getAll(Pageable pageable) {
        return repository
                .findAll(pageable)
                .map(EmployeeMapper::toResponse);
    }

    public EmployeeResponse getById(Long id) {
        Employee employee = getEmployeeByIdOrThrow(id);

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
        Employee employee = getEmployeeByIdOrThrow(id);

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

        return EmployeeMapper.toResponse(employee);
    }

    public List<WorkingHoursResponse> getWorkingHoursByEmployeeId(
            Long employeeId
    ) {
        Employee employee = getEmployeeByIdOrThrow(employeeId);

        return employee.getWorkingHours()
                .stream()
                .map(EmployeeWorkingHoursMapper::toResponse)
                .toList();
    }

    public void replaceWorkingHours(
            Long employeeId,
            List<WorkingHoursRequest> requests
    ) {

        Employee employee = getEmployeeByIdOrThrow(employeeId);

        validateWorkingHours(requests);

        List<EmployeeWorkingHours> workingHours = requests.stream()
                .map(request -> new EmployeeWorkingHours(
                        request.dayOfWeek(),
                        request.startTime(),
                        request.endTime()
                )).toList();

        employee.replaceWorkingHours(workingHours);
    }

    private Employee getEmployeeByIdOrThrow(
            Long employeeId
    ) {
        return repository
                .findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee", employeeId));
    }

    private void validateWorkingHours(List<WorkingHoursRequest> requests) {
        Set<DayOfWeek> days = new HashSet<>();

        for (WorkingHoursRequest hours: requests) {
            if (!hours.endTime().isAfter(hours.startTime())) {
                throw new IllegalArgumentException(
                        "Working hours end time must be after start time"
                );
            }

            if (!days.add(hours.dayOfWeek())) {
                throw new IllegalArgumentException(
                        "Duplicate working day: " + hours.dayOfWeek()
                );
            }
        }
    }
}
