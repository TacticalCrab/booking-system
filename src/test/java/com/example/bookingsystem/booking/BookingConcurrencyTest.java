package com.example.bookingsystem.booking;

import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import com.example.bookingsystem.user.UserRole;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
@ActiveProfiles("test")
public class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;
    private ServiceEntity service;
    private User user;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        employeeRepository.deleteAll();
        serviceRepository.deleteAll();

        user = userRepository.save(
                new User(
                     "customer@example.com",
                     "whatever",
                     "Customer",
                     UserRole.CUSTOMER
                )
        );

        service = serviceRepository.save(
                new ServiceEntity(
                        "Shave",
                        "Get your head shaved really quickly!",
                        20,
                        BigDecimal.valueOf(999.0)
                )
        );

        Employee createdEmployee = new Employee(
                "Employee",
                "employee@example.com",
                List.of(service)
        );

        buildMockWeekWorkingHours()
                .forEach(createdEmployee::addWorkingHours);

        employee = employeeRepository.save(createdEmployee);
    }

    private List<EmployeeWorkingHours> buildMockWeekWorkingHours() {
        return Arrays.stream(DayOfWeek.values())
                .map((dayOfWeek) -> new EmployeeWorkingHours(
                        dayOfWeek,
                        LocalTime.of(0, 0),
                        LocalTime.of(23, 59)
                ))
                .toList();
    }

    @Test
    void shouldNotCreateTwoOverlappingBookings() throws Exception {
        String email = user.getEmail();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 8, 10, 0)
        );

        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            Callable<Object> bookingAttempt = () -> {
                ready.countDown();

                start.await();

                try {
                    return bookingService.create(email, request);
                } catch (Exception e) {
                    return e;
                }
            };

            Future<Object> first = executorService.submit(bookingAttempt);
            Future<Object> second = executorService.submit(bookingAttempt);

            ready.await();
            start.countDown();

            Object result1 = first.get();
            Object result2 = second.get();

            executorService.shutdown();

            System.out.println("Result 1: " + result1);
            System.out.println("Result 2: " + result2);
        }

        long count = bookingRepository.countByEmployeeIdAndStatus(
                employee.getId(),
                BookingStatus.CONFIRMED
        );

        System.out.println("Confirmed bookings: " + count);

        assert count == 1;
    }
}
