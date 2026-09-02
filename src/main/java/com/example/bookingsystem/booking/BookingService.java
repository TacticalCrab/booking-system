package com.example.bookingsystem.booking;

import com.example.bookingsystem.booking.dto.BookingResponse;
import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import jakarta.transaction.Transactional;
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
            ServiceRepository serviceRepository) {
        repository = bookingRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<BookingResponse> getAll() {
        return repository
                .findAll()
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    public BookingResponse getById(Long id) {
        Booking booking = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Booking", id));

        return BookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest request) {
        User user = userRepository
                .findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User", request.userId()));

        Employee employee = employeeRepository
                .findById(request.employeeId())
                .orElseThrow(() -> new NotFoundException("Employee", request.employeeId()));

        ServiceEntity service = serviceRepository
                .findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Service", request.serviceId()));

        Booking booking = new Booking(
                user,
                employee,
                service,
                request.startTime(),
                request.endTime(),
                request.status(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Booking savedBooking = repository.save(booking);

        return BookingMapper.toResponse(savedBooking);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
