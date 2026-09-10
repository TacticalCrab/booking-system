package com.example.bookingsystem.employee;

import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.service.ServiceEntity;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name="employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany
    @JoinTable(
            name = "employee_services",
            joinColumns = @JoinColumn(name="employee_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ServiceEntity> services = new ArrayList<>();

    @OneToMany(
            mappedBy = "employee",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<EmployeeWorkingHours> workingHours = new ArrayList<>();

    protected Employee() {}

    public Employee(
            String name,
            String email,
            List<ServiceEntity> services
    ) {
        this.name = name;
        this.email = email;
        this.services = new ArrayList<>(services);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ServiceEntity> getServices() {
        return services;
    }

    public void setServices(List<ServiceEntity> services) {
        this.services = services;
    }

    public List<EmployeeWorkingHours> getWorkingHours() {
        return workingHours.stream()
                .sorted(Comparator.comparing(
                        EmployeeWorkingHours::getDayOfWeek
                ))
                .toList();
    }

    public Optional<EmployeeWorkingHours> getWorkingHoursFor(DayOfWeek dayOfWeek) {
        return workingHours.stream()
                .filter(hours -> hours.getDayOfWeek() == dayOfWeek)
                .findFirst();
    }

    public boolean isWorkingOnDay(DayOfWeek dayOfWeek) {
        return workingHours.stream()
                .anyMatch(hours -> hours.getDayOfWeek() == dayOfWeek);
    }

    public void addWorkingHours(EmployeeWorkingHours hours) {
        workingHours.add(hours);
        hours.setEmployee(this);
    }

    public void removeWorkingHours(EmployeeWorkingHours hours) {
        workingHours.remove(hours);
        hours.setEmployee(null);
    }

    public void replaceWorkingHours(List<EmployeeWorkingHours> newWorkingHours) {
        workingHours.clear();
        newWorkingHours.forEach(this::addWorkingHours);
    }

    public boolean providesService(ServiceEntity service) {
        return services.contains(service);
    }
}
