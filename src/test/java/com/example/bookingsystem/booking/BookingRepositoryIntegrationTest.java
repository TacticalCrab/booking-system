package com.example.bookingsystem.booking;

import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.support.IntegrationTest;
import com.example.bookingsystem.support.TestDataFactory;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingRepositoryIntegrationTest
        extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User user;
    private ServiceEntity service;
    private Employee employee;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        user = TestDataFactory.userBuilder()
                .withId(null)
                .build();

        user = userRepository.save(user);

        service = TestDataFactory.serviceBuilder()
                .withId(null)
                .build();

        service = serviceRepository.save(service);

        employee = TestDataFactory.employeeBuilder()
                .withId(null)
                .withWorkingHours(
                        TestDataFactory.workingHoursAllWeekWithoutIds()
                )
                .withServices(
                        new ArrayList<>(List.of(service))
                )
                .build();

        employee = employeeRepository.save(employee);
    }

    @Test
    void shouldReturnTrueWhenBookingOverlaps() {
        LocalDateTime existingStart =
                LocalDateTime.of(2026, 9, 7, 10, 0);

        LocalDateTime existingEnd =
                LocalDateTime.of(2026, 9, 7, 10, 30);

        Booking existingBooking = TestDataFactory.bookingBuilder()
                .withId(null)
                .withUser(user)
                .withEmployee(employee)
                .withService(service)
                .withStartTime(existingStart)
                .withEndTime(existingEnd)
                .withStatus(BookingStatus.CONFIRMED)
                .build();

        bookingRepository.saveAndFlush(existingBooking);

        LocalDateTime requestedStart =
                LocalDateTime.of(2026, 9, 7, 10, 15);

        LocalDateTime requestedEnd =
                LocalDateTime.of(2026, 9, 7, 10, 40);

        boolean overlaps = bookingRepository.existsOverlappingBooking(
                employee.getId(),
                requestedStart,
                requestedEnd
        );

        assertTrue(overlaps);
    }

    @Test
    void shouldReturnFalseWhenRequestedBookingEndsExactlyWhenExistingBookingStarts() {
        LocalDateTime existingStart =
                LocalDateTime.of(2026, 9, 7, 15, 0);

        LocalDateTime existingEnd =
                LocalDateTime.of(2026, 9, 7, 15, 30);

        Booking existingBooking = TestDataFactory.bookingBuilder()
                .withId(null)
                .withUser(user)
                .withEmployee(employee)
                .withService(service)
                .withStartTime(existingStart)
                .withEndTime(existingEnd)
                .withStatus(BookingStatus.CONFIRMED)
                .build();

        bookingRepository.saveAndFlush(existingBooking);

        LocalDateTime requestedStart =
                LocalDateTime.of(2026, 9, 7, 10, 15);

        LocalDateTime requestedEnd =
                LocalDateTime.of(2026, 9, 7, 15, 0);

        boolean overlaps = bookingRepository.existsOverlappingBooking(
                employee.getId(),
                requestedStart,
                requestedEnd
        );

        assertFalse(overlaps);
    }

    @Test
    void shouldReturnFalseWhenRequestedBookingStartsExactlyWhenExistingBookingEnds() {
        LocalDateTime existingStart =
                LocalDateTime.of(2026, 9, 7, 15, 0);

        LocalDateTime existingEnd =
                LocalDateTime.of(2026, 9, 7, 15, 30);

        Booking existingBooking = TestDataFactory.bookingBuilder()
                .withId(null)
                .withUser(user)
                .withEmployee(employee)
                .withService(service)
                .withStartTime(existingStart)
                .withEndTime(existingEnd)
                .withStatus(BookingStatus.CONFIRMED)
                .build();

        bookingRepository.saveAndFlush(existingBooking);

        LocalDateTime requestedStart =
                LocalDateTime.of(2026, 9, 7, 15, 30);

        LocalDateTime requestedEnd =
                LocalDateTime.of(2026, 9, 7, 16, 0);

        boolean overlaps = bookingRepository.existsOverlappingBooking(
                employee.getId(),
                requestedStart,
                requestedEnd
        );

        assertFalse(overlaps);
    }

    @Test
    void shouldReturnFalseWhenOverlappingBookingIsCancelled() {
        LocalDateTime existingStart =
                LocalDateTime.of(2026, 9, 7, 10, 0);

        LocalDateTime existingEnd =
                LocalDateTime.of(2026, 9, 7, 10, 30);

        Booking existingBooking = TestDataFactory.bookingBuilder()
                .withId(null)
                .withUser(user)
                .withEmployee(employee)
                .withService(service)
                .withStartTime(existingStart)
                .withEndTime(existingEnd)
                .withStatus(BookingStatus.CANCELLED)
                .build();

        bookingRepository.saveAndFlush(existingBooking);

        LocalDateTime requestedStart =
                LocalDateTime.of(2026, 9, 7, 10, 15);

        LocalDateTime requestedEnd =
                LocalDateTime.of(2026, 9, 7, 10, 40);

        boolean overlaps = bookingRepository.existsOverlappingBooking(
                employee.getId(),
                requestedStart,
                requestedEnd
        );

        assertFalse(overlaps);
    }

    @Test
    void shouldReturnFalseWhenOverlappingBookingBelongsToDifferentEmployee() {
        LocalDateTime existingStart =
                LocalDateTime.of(2026, 9, 7, 10, 0);

        LocalDateTime existingEnd =
                LocalDateTime.of(2026, 9, 7, 10, 30);

        Booking existingBooking = TestDataFactory.bookingBuilder()
                .withId(null)
                .withUser(user)
                .withEmployee(employee)
                .withService(service)
                .withStartTime(existingStart)
                .withEndTime(existingEnd)
                .withStatus(BookingStatus.CONFIRMED)
                .build();

        bookingRepository.saveAndFlush(existingBooking);

        Employee otherEmployee = TestDataFactory.employeeBuilder()
                .withId(null)
                .withEmail("employee2@example.com")
                .withWorkingHours(
                        TestDataFactory.workingHoursAllWeekWithoutIds()
                )
                .withServices(
                        new ArrayList<>(List.of(service))
                )
                .build();

        otherEmployee = employeeRepository.save(otherEmployee);

        LocalDateTime requestedStart =
                LocalDateTime.of(2026, 9, 7, 10, 15);

        LocalDateTime requestedEnd =
                LocalDateTime.of(2026, 9, 7, 10, 40);

        boolean overlaps = bookingRepository.existsOverlappingBooking(
                otherEmployee.getId(),
                requestedStart,
                requestedEnd
        );

        assertFalse(overlaps);
    }
}
