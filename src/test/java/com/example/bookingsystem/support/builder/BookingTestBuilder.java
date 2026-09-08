package com.example.bookingsystem.support.builder;

import com.example.bookingsystem.booking.Booking;
import com.example.bookingsystem.booking.BookingStatus;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.support.TestDataFactory;
import com.example.bookingsystem.user.User;

import java.time.LocalDateTime;

public class BookingTestBuilder {

    private User user = TestDataFactory.customer();
    private Employee employee = TestDataFactory.employeeWithServices();
    private ServiceEntity service = employee.getServices().getFirst();
    private LocalDateTime startTime = LocalDateTime.of(2026, 9, 7, 10, 0);
    private LocalDateTime endTime =
            startTime.plusMinutes(service.getDurationMinutes());
    private BookingStatus status = BookingStatus.CONFIRMED;
    private LocalDateTime createdAt = LocalDateTime.of(2026, 9, 1, 12, 0);
    private LocalDateTime updatedAt = LocalDateTime.of(2026, 9, 1, 12, 0);
    private Long id = 1L;

    public BookingTestBuilder withUser(User user) {
        this.user = user;
        return this;
    }

    public BookingTestBuilder withEmployee(Employee employee) {
        this.employee = employee;
        return this;
    }

    public BookingTestBuilder withService(ServiceEntity service) {
        this.service = service;
        this.endTime = startTime.plusMinutes(service.getDurationMinutes());
        return this;
    }

    public BookingTestBuilder withStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes(service.getDurationMinutes());
        return this;
    }

    public BookingTestBuilder withEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public BookingTestBuilder withStatus(BookingStatus status) {
        this.status = status;
        return this;
    }

    public BookingTestBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public BookingTestBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public BookingTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public BookingTestBuilder withoutId() {
        this.id = null;
        return this;
    }

    public Booking build() {
        Booking booking = new Booking(
                user,
                employee,
                service,
                startTime,
                endTime,
                status,
                createdAt,
                updatedAt
        );

        booking.setId(id);

        return booking;
    }
}
