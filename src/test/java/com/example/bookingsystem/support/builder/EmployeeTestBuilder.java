package com.example.bookingsystem.support.builder;

import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.support.TestDataFactory;

import java.util.List;

public class EmployeeTestBuilder {

    private String name = "Top Employee";
    private String email = "test-employee@example.com";
    private List<ServiceEntity> services = TestDataFactory.services(10);
    private List<EmployeeWorkingHours> workingHours =
            TestDataFactory.workingHoursAllWeek();
    private Long id = 1L;

    public EmployeeTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public EmployeeTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public EmployeeTestBuilder withServices(List<ServiceEntity> services) {
        this.services = services;
        return this;
    }

    public EmployeeTestBuilder withoutServices() {
        this.services = List.of();
        return this;
    }

    public EmployeeTestBuilder withWorkingHours(
            List<EmployeeWorkingHours> workingHours
    ) {
        this.workingHours = workingHours;
        return this;
    }

    public EmployeeTestBuilder withoutWorkingHours() {
        this.workingHours = List.of();
        return this;
    }

    public EmployeeTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public EmployeeTestBuilder withoutId() {
        this.id = null;
        return this;
    }

    public Employee build() {
        Employee employee = new Employee(
                name,
                email,
                services
        );

        employee.replaceWorkingHours(workingHours);
        employee.setId(id);

        return employee;
    }
}
