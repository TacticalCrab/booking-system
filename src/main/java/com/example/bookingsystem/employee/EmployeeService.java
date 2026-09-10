package com.example.bookingsystem.employee;

import com.example.bookingsystem.booking.Booking;
import com.example.bookingsystem.booking.BookingRepository;
import com.example.bookingsystem.booking.BookingStatus;
import com.example.bookingsystem.cache.availability.AvailabilityEventPublisher;
import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.employee.dto.*;
import com.example.bookingsystem.employee.exception.InvalidEmployeeServiceException;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHoursMapper;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.bookingsystem.cache.CacheNames.EMPLOYEE_AVAILABILITY;

@Service
class EmployeeService {

    private final EmployeeRepository repository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityEventPublisher availabilityEventPublisher;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            ServiceRepository serviceRepository,
            BookingRepository bookingRepository,
            AvailabilityEventPublisher availabilityEventPublisher
    ) {
        repository = employeeRepository;
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityEventPublisher = availabilityEventPublisher;
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

    @Cacheable(
            value = EMPLOYEE_AVAILABILITY,
            key = "#employeeId + ':' + #serviceId + ':' + #date"
    )
    public EmployeeAvailabilityResponse getAvailability(
        Long employeeId,
        Long serviceId,
        LocalDate date
    ) {
        Employee employee = getEmployeeByIdOrThrow(employeeId);
        ServiceEntity service = getServiceByIdOrThrow(serviceId);

        if (!employee.providesService(service)) {
            throw new InvalidEmployeeServiceException("Employee does not provide this service");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<EmployeeWorkingHours> optionalWorkingHours = employee.getWorkingHoursFor(dayOfWeek);

        if (optionalWorkingHours.isEmpty()) {
            return new EmployeeAvailabilityResponse(
                    employeeId,
                    serviceId,
                    date,
                    List.of()
            );
        }

        EmployeeWorkingHours workingHours = optionalWorkingHours.get();

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<Booking> bookings = bookingRepository.findForEmployeeOnDay(
                employeeId,
                dayStart,
                dayEnd,
                BookingStatus.CANCELLED
        );

        LocalDateTime workingStart =
                date.atTime(workingHours.getStartTime());

        LocalDateTime workingEnd =
                date.atTime(workingHours.getEndTime());

        int serviceDuration = service.getDurationMinutes();

        List<LocalTime> slots = new ArrayList<>();

        for (
                LocalDateTime slotStart = workingStart;
                !slotStart.plusMinutes(serviceDuration).isAfter(workingEnd);
                slotStart = slotStart.plusMinutes(30)
        ) {
            LocalDateTime candidateStart = slotStart;
            LocalDateTime candidateEnd =
                    slotStart.plusMinutes(serviceDuration);

            boolean overlaps = bookings.stream()
                    .anyMatch(booking ->
                            booking.getStartTime().isBefore(candidateEnd)
                            && booking.getEndTime().isAfter(candidateStart)
                    );

            if (!overlaps) {
                slots.add(candidateStart.toLocalTime());
            }
        }

        return new EmployeeAvailabilityResponse(
                employeeId,
                serviceId,
                date,
                slots
        );
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

        availabilityEventPublisher
                .availabilityChanged(employee.getId());

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

    @Transactional
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

        availabilityEventPublisher
                .availabilityChanged(employee.getId());
    }

    private Employee getEmployeeByIdOrThrow(
            Long employeeId
    ) {
        return repository
                .findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee", employeeId));
    }

    private ServiceEntity getServiceByIdOrThrow(
            Long serviceId
    ) {
        return serviceRepository
                .findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service", serviceId));
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
