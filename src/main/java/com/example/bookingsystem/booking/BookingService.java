package com.example.bookingsystem.booking;

import com.example.bookingsystem.auth.exception.AccessDeniedException;
import com.example.bookingsystem.booking.dto.BookingResponse;
import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import com.example.bookingsystem.booking.event.BookingCreatedEvent;
import com.example.bookingsystem.booking.exception.BookingConflictException;
import com.example.bookingsystem.booking.exception.InvalidBookingException;
import com.example.bookingsystem.cache.availability.AvailabilityEventPublisher;
import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.idempotency.BookingRequestHasher;
import com.example.bookingsystem.idempotency.IdempotencyRecord;
import com.example.bookingsystem.idempotency.IdempotencyService;
import com.example.bookingsystem.idempotency.IdempotencyStatus;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
class BookingService {
    private static final int SLOT_INTERVAL_MINUTES = 30;

    private static final Logger log =
            LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository repository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;
    private final AvailabilityEventPublisher availabilityEventPublisher;
    private final BookingEventPublisher bookingEventPublisher;
    private final IdempotencyService idempotencyService;
    private final BookingRequestHasher bookingRequestHasher;

    BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            ServiceRepository serviceRepository,
            AvailabilityEventPublisher availabilityEventPublisher,
            BookingEventPublisher bookingEventPublisher, IdempotencyService idempotencyService, BookingRequestHasher bookingRequestHasher
    ) {
        repository = bookingRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityEventPublisher = availabilityEventPublisher;
        this.bookingEventPublisher = bookingEventPublisher;
        this.idempotencyService = idempotencyService;
        this.bookingRequestHasher = bookingRequestHasher;
    }

    public Page<BookingResponse> getAll(Pageable pageable) {
        return repository
                .findAll(pageable)
                .map(BookingMapper::toResponse);
    }

    public BookingResponse getById(
            Long id,
            String authenticatedUserEmail
    ) {
        Booking booking = getAuthorizedBooking(id, authenticatedUserEmail);

        return BookingMapper.toResponse(booking);
    }

    public Page<BookingResponse> getByUserEmail(String email, Pageable pageable) {
        return repository
                .findAllByUserEmail(email, pageable)
                .map(BookingMapper::toResponse);
    }

    @Transactional
    public BookingResponse create(
            String email,
            String idempotencyKey,
            CreateBookingRequest request
    ) {
        User user = getUserByEmailOrThrow(email);

        String requestHash = bookingRequestHasher.hash(request);

        IdempotencyRecord idempotencyRecord = idempotencyService.reserve(
                user.getId(),
                idempotencyKey,
                requestHash
        );

        if (idempotencyRecord.getStatus() == IdempotencyStatus.COMPLETED) {
            return getPreviousBookingResponse(idempotencyRecord);
        }

        Employee employee = getEmployeeByIdForUpdateOrThrow(request.employeeId());
        ServiceEntity service = getServiceByIdOrThrow(request.serviceId());

        validateEmployeeProvidesService(employee, service);

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = calculateEndTime(startTime, service);

        validateWorkingHours(employee, startTime, endTime);
        validateSlotInterval(employee, startTime);
        validateOverlap(employee, startTime, endTime);

        LocalDateTime now = LocalDateTime.now();

        Booking booking = new Booking(
                user,
                employee,
                service,
                request.startTime(),
                endTime,
                BookingStatus.CONFIRMED,
                now,
                now
        );

        Booking savedBooking = repository.save(booking);

        LocalDate bookingDate = booking.getStartTime().toLocalDate();
        availabilityEventPublisher
                .availabilityChanged(
                        employee,
                        bookingDate
                );

        bookingEventPublisher
                .publishCreated(savedBooking);

        idempotencyRecord.complete(booking.getId());

        log.info("Booking created: bookingId={}", booking.getId());

        return BookingMapper.toResponse(savedBooking);
    }

    public BookingResponse updateStatus(Long id, BookingStatus status) {
        Booking booking = getBookingByIdOrThrow(id);

        booking.setStatus(status);
        Booking savedBooking = repository.save(booking);

        availabilityEventPublisher
                .availabilityChanged(
                        savedBooking.getEmployee(),
                        savedBooking.getStartTime().toLocalDate()
                );

        return BookingMapper.toResponse(savedBooking);
    }

    public BookingResponse cancel(
            Long id,
            String authenticatedUserEmail
    ) {
        Booking booking = getAuthorizedBooking(id, authenticatedUserEmail);
        booking.cancel();

        Booking savedBooking = repository.save(booking);
        availabilityEventPublisher
                .availabilityChanged(
                        savedBooking.getEmployee(),
                        savedBooking.getStartTime().toLocalDate()
                );

        return BookingMapper.toResponse(savedBooking);
    }

    public void delete(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        repository.delete(booking);

        availabilityEventPublisher
                .availabilityChanged(
                        booking.getEmployee(),
                        booking.getStartTime().toLocalDate()
                );
    }

    private Booking getAuthorizedBooking(
            Long id,
            String authenticatedUserEmail
    ) {
        User currentUser = userRepository
                .findByEmail(authenticatedUserEmail)
                .orElseThrow(AccessDeniedException::new);

        Booking booking = getBookingByIdOrThrow(id);

        if (!booking.isOwnedBy(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new AccessDeniedException();
        }

        return booking;
    }

    private Booking getBookingByIdOrThrow(Long bookingId) {
        return repository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking", bookingId));
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User", "email", email));

    }

    private Employee getEmployeeByIdOrThrow(Long employeeId) {
        return employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee", employeeId));

    }

    private Employee getEmployeeByIdForUpdateOrThrow(Long employeeId) {
        return employeeRepository
                .findByIdForUpdate(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee", employeeId));
    }

    private ServiceEntity getServiceByIdOrThrow(Long serviceId) {
        return serviceRepository
                .findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service", serviceId));
    }

    private BookingResponse getPreviousBookingResponse(
            IdempotencyRecord idempotencyRecord
    ) {
        Booking booking = getBookingByIdOrThrow(idempotencyRecord.getBookingId());

        return BookingMapper.toResponse(booking);
    }

    private void validateEmployeeProvidesService(
            Employee employee,
            ServiceEntity service
    ) {
        if (!employee.providesService(service)) {
            throw new InvalidBookingException(
                    "Employee does not provide this service"
            );
        }
    }

    private void validateWorkingHours(
            Employee employee,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        DayOfWeek dayOfWeek = startTime.getDayOfWeek();

        EmployeeWorkingHours workingHours = employee
                .getWorkingHoursFor(dayOfWeek)
                .orElseThrow(() -> new InvalidBookingException(
                        "Employee is not working on this day"
                ));

        if (!workingHours.contains(
                startTime,
                endTime
        )) {
            throw new InvalidBookingException(
                    "Booking is outside of working hours"
            );
        }
    }

    private void validateOverlap(
            Employee employee,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        boolean bookingOverlaps = repository
                .existsOverlappingBooking(
                        employee.getId(),
                        startTime,
                        endTime
                );

        if (bookingOverlaps) {
            throw new BookingConflictException();
        }
    }

    private void validateSlotInterval(
            Employee employee,
            LocalDateTime requestedStart
    ) {

        DayOfWeek dayOfWeek = requestedStart.getDayOfWeek();
        EmployeeWorkingHours employeeWorkingHours =
                getEmployeeWorkingHoursForDayOrThrow(
                        employee,
                        dayOfWeek
                );

        LocalDateTime workingStart = requestedStart
                .toLocalDate()
                .atTime(employeeWorkingHours.getStartTime());

        long minutesFromStart = Duration.between(
                workingStart,
                requestedStart
        ).toMinutes();

        if (
                requestedStart.getSecond() != 0 ||
                requestedStart.getNano() != 0 ||
                minutesFromStart % SLOT_INTERVAL_MINUTES != 0
        ) {
            throw new InvalidBookingException(
                    "Booking must start on valid time slot"
            );
        }
    }

    private LocalDateTime calculateEndTime(
            LocalDateTime startTime,
            ServiceEntity service
    ) {
        return startTime.plusMinutes(service.getDurationMinutes());
    }

    private EmployeeWorkingHours getEmployeeWorkingHoursForDayOrThrow(
            Employee employee,
            DayOfWeek dayOfWeek
    ) {

        return employee
                .getWorkingHoursFor(dayOfWeek)
                .orElseThrow(() -> new InvalidBookingException(
                        "Employee is not working on this day"
                ));
    }
}
