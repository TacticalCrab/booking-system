package com.example.bookingsystem.employee;

import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EmployeeWorkingHoursTest {
    @Test
    void shouldReturnTrueWhenContainTimeIsInsideWorkingHours() {
        EmployeeWorkingHours employeeWorkingHours = new EmployeeWorkingHours(
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(20, 0)
        );

        LocalDateTime startTime = LocalDateTime.of(2026, 9, 7, 11, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 7, 15, 0);

        assertTrue(
                employeeWorkingHours.contains(
                        startTime,
                        endTime
                )
        );
    }

    @Test
    void shouldReturnTrueWhenTimeStartsExactlyAtWorkingHoursStart() {
        EmployeeWorkingHours employeeWorkingHours = new EmployeeWorkingHours(
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(20, 0)
        );

        LocalDateTime startTime = LocalDateTime.of(2026, 9, 7, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 7, 15, 0);

        assertTrue(
                employeeWorkingHours.contains(startTime, endTime)
        );
    }

    @Test
    void shouldReturnTrueWhenTimeEndsExactlyAtWorkingHoursEnd() {
        EmployeeWorkingHours employeeWorkingHours = new EmployeeWorkingHours(
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(20, 0)
        );

        LocalDateTime startTime = LocalDateTime.of(2026, 9, 7, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 7, 20, 0);

        assertTrue(
                employeeWorkingHours.contains(startTime, endTime)
        );
    }

    @Test
    void shouldReturnFalseWhenTimeIsOutsideWorkingHours() {
        EmployeeWorkingHours employeeWorkingHours = new EmployeeWorkingHours(
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(20, 0)
        );

        LocalDateTime startTime = LocalDateTime.of(2026, 9, 7, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 7, 15, 0);

        assertFalse(
                employeeWorkingHours.contains(
                        startTime,
                        endTime
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsAfterEndTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmployeeWorkingHours(
                        DayOfWeek.MONDAY,
                        LocalTime.of(20, 0),
                        LocalTime.of(10, 0)
                )
        );
    }
}
