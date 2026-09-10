package com.example.bookingsystem.employee;

import com.example.bookingsystem.booking.Booking;
import com.example.bookingsystem.booking.BookingRepository;
import com.example.bookingsystem.booking.BookingStatus;
import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.employee.dto.EmployeeAvailabilityResponse;
import com.example.bookingsystem.employee.exception.InvalidEmployeeServiceException;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 7);

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void shouldReturnHalfHourlySlotsThatFitEntirelyWithinWorkingHours() {
        ServiceEntity service = serviceWithDuration(30);
        Employee employee = employeeFor(service, LocalTime.of(9, 0), LocalTime.of(10, 30));
        stubEmployeeAndService(employee, service);
        when(bookingRepository.findForEmployeeOnDay(anyLong(), any(), any(), any()))
                .thenReturn(List.of());

        EmployeeAvailabilityResponse response = employeeService.getAvailability(
                employee.getId(), service.getId(), MONDAY);

        assertEquals(employee.getId(), response.employeeId());
        assertEquals(service.getId(), response.serviceId());
        assertEquals(MONDAY, response.date());
        assertEquals(List.of(
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0)
        ), response.slots());
    }

    @Test
    void shouldExcludeEverySlotThatOverlapsAnActiveBookingButKeepAdjacentSlots() {
        ServiceEntity service = serviceWithDuration(30);
        Employee employee = employeeFor(service, LocalTime.of(9, 0), LocalTime.of(11, 0));
        Booking booking = TestDataFactory.bookingBuilder()
                .withEmployee(employee)
                .withService(service)
                .withStartTime(MONDAY.atTime(9, 30))
                .withEndTime(MONDAY.atTime(10, 0))
                .build();
        stubEmployeeAndService(employee, service);
        when(bookingRepository.findForEmployeeOnDay(anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));

        EmployeeAvailabilityResponse response = employeeService.getAvailability(
                employee.getId(), service.getId(), MONDAY);

        assertEquals(List.of(
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30)
        ), response.slots());
    }

    @Test
    void shouldReturnNoSlotsWhenEmployeeDoesNotWorkOnRequestedDay() {
        ServiceEntity service = serviceWithDuration(30);
        Employee employee = TestDataFactory.employeeBuilder()
                .withServices(List.of(service))
                .withWorkingHours(List.of(TestDataFactory.workingHoursBuilder()
                        .withDayOfWeek(DayOfWeek.TUESDAY)
                        .withStartTime(LocalTime.of(9, 0))
                        .withEndTime(LocalTime.of(17, 0))
                        .build()))
                .build();
        stubEmployeeAndService(employee, service);

        EmployeeAvailabilityResponse response = employeeService.getAvailability(
                employee.getId(), service.getId(), MONDAY);

        assertEquals(List.of(), response.slots());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void shouldRejectServiceNotProvidedByEmployeeBeforeLookingUpBookings() {
        ServiceEntity service = serviceWithDuration(30);
        Employee employee = TestDataFactory.employeeBuilder()
                .withoutServices()
                .build();
        stubEmployeeAndService(employee, service);

        assertThrows(InvalidEmployeeServiceException.class, () ->
                employeeService.getAvailability(employee.getId(), service.getId(), MONDAY));

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void shouldQueryActiveBookingsAcrossTheWholeRequestedDay() {
        ServiceEntity service = serviceWithDuration(30);
        Employee employee = employeeFor(service, LocalTime.of(9, 0), LocalTime.of(10, 0));
        stubEmployeeAndService(employee, service);
        when(bookingRepository.findForEmployeeOnDay(anyLong(), any(), any(), any()))
                .thenReturn(List.of());

        employeeService.getAvailability(employee.getId(), service.getId(), MONDAY);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(bookingRepository).findForEmployeeOnDay(
                eq(employee.getId()), startCaptor.capture(), endCaptor.capture(),
                eq(BookingStatus.CANCELLED));
        assertEquals(MONDAY.atStartOfDay(), startCaptor.getValue());
        assertEquals(MONDAY.plusDays(1).atStartOfDay(), endCaptor.getValue());
    }

    @Test
    void shouldThrowWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                employeeService.getAvailability(99L, 1L, MONDAY));

        assertEquals("Employee", exception.getResource());
        verifyNoInteractions(serviceRepository, bookingRepository);
    }

    private ServiceEntity serviceWithDuration(int durationMinutes) {
        return TestDataFactory.serviceBuilder()
                .withId(2L)
                .withDurationMinutes(durationMinutes)
                .build();
    }

    private Employee employeeFor(ServiceEntity service, LocalTime start, LocalTime end) {
        EmployeeWorkingHours hours = TestDataFactory.workingHoursBuilder()
                .withDayOfWeek(DayOfWeek.MONDAY)
                .withStartTime(start)
                .withEndTime(end)
                .build();
        return TestDataFactory.employeeBuilder()
                .withServices(List.of(service))
                .withWorkingHours(List.of(hours))
                .build();
    }

    private void stubEmployeeAndService(Employee employee, ServiceEntity service) {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
    }
}
