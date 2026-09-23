package com.example.bookingsystem.employee;

import com.example.bookingsystem.employee.exception.InvalidWorkingHoursException;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.service.ServiceEntity;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public void replaceWorkingHours(
            List<EmployeeWorkingHours> newWorkingHours
    ) {
        Map<DayOfWeek, EmployeeWorkingHours> newByDay =
                mapWorkingHoursByDay(newWorkingHours);

        List<EmployeeWorkingHours> existingWorkingHours =
                new ArrayList<>(workingHours);

        for (EmployeeWorkingHours existing : existingWorkingHours) {
            EmployeeWorkingHours replacement =
                    newByDay.remove(existing.getDayOfWeek());

            if (replacement == null) {
                removeWorkingHours(existing);
                continue;
            }

            existing.updateHours(
                    replacement.getStartTime(),
                    replacement.getEndTime()
            );
        }

        newByDay.values()
                .forEach(this::addWorkingHours);
    }


    public boolean providesService(ServiceEntity service) {
        return services.contains(service);
    }

    private Map<DayOfWeek, EmployeeWorkingHours> mapWorkingHoursByDay(
            List<EmployeeWorkingHours> workingHours
    ) {
        Map<DayOfWeek, EmployeeWorkingHours> workingHoursByDay =
                new EnumMap<>(DayOfWeek.class);

        for (EmployeeWorkingHours hours : workingHours) {
            EmployeeWorkingHours duplicate =
                    workingHoursByDay.putIfAbsent(
                            hours.getDayOfWeek(),
                            hours
                    );

            if (duplicate != null) {
                throw new InvalidWorkingHoursException(
                        "Duplicate working day: " +
                                hours.getDayOfWeek()
                );
            }
        }

        return workingHoursByDay;
    }
}
