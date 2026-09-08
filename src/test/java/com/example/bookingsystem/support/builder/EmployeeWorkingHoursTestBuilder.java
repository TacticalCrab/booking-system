package com.example.bookingsystem.support.builder;

import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class EmployeeWorkingHoursTestBuilder {

    private DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
    private LocalTime startTime = LocalTime.of(9, 0);
    private LocalTime endTime = LocalTime.of(17, 0);
    private Employee employee;
    private Long id = 1L;

    public EmployeeWorkingHoursTestBuilder withDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        return this;
    }

    public EmployeeWorkingHoursTestBuilder withStartTime(LocalTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public EmployeeWorkingHoursTestBuilder withEndTime(LocalTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public EmployeeWorkingHoursTestBuilder withEmployee(Employee employee) {
        this.employee = employee;
        return this;
    }

    public EmployeeWorkingHoursTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public EmployeeWorkingHoursTestBuilder withoutId() {
        this.id = null;
        return this;
    }

    public EmployeeWorkingHours build() {
        EmployeeWorkingHours workingHours = new EmployeeWorkingHours(
                dayOfWeek,
                startTime,
                endTime
        );

        workingHours.setEmployee(employee);
        ReflectionTestUtils.setField(workingHours, "id", id);

        return workingHours;
    }
}
