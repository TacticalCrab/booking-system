package com.example.bookingsystem.booking;

import com.example.bookingsystem.auth.exception.AccessDeniedException;
import com.example.bookingsystem.auth.exception.AuthenticationFailedException;
import com.example.bookingsystem.booking.dto.BookingResponse;
import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import com.example.bookingsystem.booking.exception.InvalidBookingException;
import com.example.bookingsystem.common.exception.InvalidTimeException;
import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
class BookingService {
    private final BookingRepository repository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;

    BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            ServiceRepository serviceRepository
    ) {
        repository = bookingRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.serviceRepository = serviceRepository;
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
    public BookingResponse create(String email, CreateBookingRequest request) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User", "email", email));

        Employee employee = employeeRepository
                .findById(request.employeeId())
                .orElseThrow(() -> new NotFoundException("Employee", request.employeeId()));

        ServiceEntity service = serviceRepository
                .findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Service", request.serviceId()));

        if (!employee.getServices().contains(service)) {
            throw new InvalidBookingException(
                    "Employee does not provide this service"
            );
        }

        LocalDateTime endTime = request.startTime()
                .plusMinutes(service.getDurationMinutes());

        Booking booking = new Booking(
                user,
                employee,
                service,
                request.startTime(),
                endTime,
                BookingStatus.CONFIRMED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Booking savedBooking = repository.save(booking);

        return BookingMapper.toResponse(savedBooking);
    }

    public BookingResponse updateStatus(Long id, BookingStatus status) {
        Booking booking = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Booking", id));

        booking.setStatus(status);
        repository.save(booking);

        return BookingMapper.toResponse(booking);
    }

    public BookingResponse cancel(
            Long id,
            String authenticatedUserEmail
    ) {
        Booking booking = getAuthorizedBooking(id, authenticatedUserEmail);
        booking.cancel();

        Booking savedBooking = repository.save(booking);

        return BookingMapper.toResponse(savedBooking);
    }

    public void delete(Long id) {
        Booking booking = repository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Booking", id));

        repository.delete(booking);
    }

    private Booking getAuthorizedBooking(
            Long id,
            String authenticatedUserEmail
    ) {
        User currentUser = userRepository
                .findByEmail(authenticatedUserEmail)
                .orElseThrow(AccessDeniedException::new);

        Booking booking = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Booking", id));

        if (!booking.isOwnedBy(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new AccessDeniedException();
        }

        return booking;
    }
}
