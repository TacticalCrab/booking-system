package com.example.bookingsystem.support;

import com.example.bookingsystem.booking.Booking;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.support.builder.BookingTestBuilder;
import com.example.bookingsystem.support.builder.EmployeeTestBuilder;
import com.example.bookingsystem.support.builder.EmployeeWorkingHoursTestBuilder;
import com.example.bookingsystem.support.builder.ServiceTestBuilder;
import com.example.bookingsystem.support.builder.UserTestBuilder;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRole;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static UserTestBuilder userBuilder() {
        return new UserTestBuilder();
    }

    public static EmployeeTestBuilder employeeBuilder() {
        return new EmployeeTestBuilder();
    }

    public static ServiceTestBuilder serviceBuilder() {
        return new ServiceTestBuilder();
    }

    public static BookingTestBuilder bookingBuilder() {
        return new BookingTestBuilder();
    }

    public static EmployeeWorkingHoursTestBuilder workingHoursBuilder() {
        return new EmployeeWorkingHoursTestBuilder();
    }

    public static User customer() {
        return user(UserRole.CUSTOMER);
    }

    public static User admin() {
        return user(UserRole.ADMIN);
    }

    public static User user(UserRole role) {
        return userBuilder()
                .withRole(role)
                .build();
    }

    public static ServiceEntity service() {
        return serviceBuilder().build();
    }

    public static List<ServiceEntity> services(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(index -> serviceBuilder()
                        .withId((long) index)
                        .withName("Service " + index)
                        .withDescription("Description of service " + index)
                        .withDurationMinutes(index * 15)
                        .withPrice(BigDecimal.valueOf(index * 10L))
                        .build())
                .toList();
    }

    public static List<EmployeeWorkingHours> workingHoursAllWeek() {
        DayOfWeek[] days = DayOfWeek.values();

        return IntStream.range(0, days.length)
                .mapToObj(index -> workingHoursBuilder()
                        .withId((long) index + 1)
                        .withDayOfWeek(days[index])
                        .withStartTime(LocalTime.of(9, 0))
                        .withEndTime(LocalTime.of(17, 0))
                        .build())
                .toList();
    }

    public static List<EmployeeWorkingHours> workingHoursAllWeekWithoutIds() {
        DayOfWeek[] days = DayOfWeek.values();

        return Arrays.stream(days).map(day -> workingHoursBuilder()
                        .withId(null)
                        .withDayOfWeek(day)
                        .withStartTime(LocalTime.of(9, 0))
                        .withEndTime(LocalTime.of(17, 0))
                        .build())
                .toList();
    }

    public static Employee employee(List<ServiceEntity> services) {
        return employeeBuilder()
                .withServices(services)
                .build();
    }

    public static Employee employeeWithServices() {
        return employee(services(10));
    }

    public static Employee employeeWithoutServices() {
        return employee(List.of());
    }

    public static Booking booking() {
        return bookingBuilder().build();
    }
}
