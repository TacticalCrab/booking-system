package com.example.bookingsystem.booking;


import com.example.bookingsystem.auth.exception.AccessDeniedException;
import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import com.example.bookingsystem.booking.exception.BookingConflictException;
import com.example.bookingsystem.booking.exception.InvalidBookingException;
import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.support.TestDataFactory;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldRejectBookingWhenEmployeeDoesNotProvideService() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withoutServices()
                .build();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 7, 9, 0)
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.of(employee));

        when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.of(service));

        assertThrows(
                InvalidBookingException.class,
                () -> bookingService.create(
                        customer.getEmail(),
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withoutServices()
                .build();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 7, 9, 0)
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(
                        customer.getEmail(),
                        request
                )
        );

        assertEquals("User", exception.getResource());
        assertEquals("email", exception.getMatcher());
        assertEquals(customer.getEmail(), exception.getValue());

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withoutServices()
                .build();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 7, 9, 0)
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(
                        customer.getEmail(),
                        request
                )
        );

        assertEquals("Employee", exception.getResource());
        assertEquals("id", exception.getMatcher());
        assertEquals(employee.getId(), exception.getValue());

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenServiceDoesNotExist() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withoutServices()
                .build();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 7, 9, 0)
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.of(employee));

        when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(
                        customer.getEmail(),
                        request
                )
        );

        assertEquals("Service", exception.getResource());
        assertEquals("id", exception.getMatcher());
        assertEquals(service.getId(), exception.getValue());

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void shouldRejectBookingWhenBookingIsOutsideWorkingHours() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withoutServices()
                .build();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 7, 8, 0)
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.of(employee));

        when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.of(service));

        assertThrows(
                InvalidBookingException.class,
                () -> bookingService.create(
                        customer.getEmail(),
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void shouldRejectBookingWhenBookingIsNotWorkingOnRequestedDay() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withoutServices()
                .withoutWorkingHours()
                .build();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 7, 8, 0)
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.of(employee));

        when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.of(service));

        assertThrows(
                InvalidBookingException.class,
                () -> bookingService.create(
                        customer.getEmail(),
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void shouldRejectBookingWhenRequestedTimeOverlapsExistingBooking() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .withDurationMinutes(30)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withServices(new ArrayList<>(List.of(service)))
                .build();

        LocalDateTime requestedStart = LocalDateTime.of(2026, 9, 7, 10, 0);
        LocalDateTime requestedEnd = requestedStart.plusMinutes(
                service.getDurationMinutes()
        );

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                requestedStart
        );

        when(bookingRepository.existsOverlappingBooking(
                employee.getId(),
                requestedStart,
                requestedEnd
        )).thenReturn(true);

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.of(employee));

        when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.of(service));

        assertThrows(
                BookingConflictException.class,
                () -> bookingService.create(
                        customer.getEmail(),
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any(Booking.class));

    }

    @Test
    void shouldCreateBookingWhenRequestIsValid() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withServices(new ArrayList<>(List.of(service)))
                .build();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 7, 9, 0)
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.of(employee));

        when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.of(service));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.create(
                customer.getEmail(),
                request
        );

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository).save(captor.capture());

        Booking savedBooking = captor.getValue();

        assertEquals(customer.getId(), savedBooking.getUser().getId());
        assertEquals(employee.getId(), savedBooking.getEmployee().getId());
        assertEquals(service.getId(), savedBooking.getService().getId());
        assertEquals(request.startTime(), savedBooking.getStartTime());

        LocalDateTime endTime = request.startTime().plusMinutes(service.getDurationMinutes());
        assertEquals(endTime, savedBooking.getEndTime());
        assertEquals(BookingStatus.CONFIRMED, savedBooking.getStatus());
    }

    @Test
    void shouldCreateBookingWhenStartTimeIsOnThirtyMinuteSlot() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withServices(new ArrayList<>(List.of(service)))
                .build();

        LocalDateTime slotStart = LocalDateTime.of(2026, 9, 7, 9, 30);
        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                slotStart
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));
        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.of(employee));
        when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.of(service));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.create(customer.getEmail(), request);

        verify(bookingRepository).save(argThat(booking ->
                booking.getStartTime().equals(slotStart)
        ));
    }

    @Test
    void shouldRejectBookingWhenStartTimeIsBetweenThirtyMinuteSlots() {
        User customer = TestDataFactory.customer();

        ServiceEntity service = TestDataFactory.serviceBuilder()
                .withId(5L)
                .build();

        Employee employee = TestDataFactory.employeeBuilder()
                .withServices(new ArrayList<>(List.of(service)))
                .build();

        CreateBookingRequest request = new CreateBookingRequest(
                employee.getId(),
                service.getId(),
                LocalDateTime.of(2026, 9, 7, 9, 15)
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));
        when(employeeRepository.findByIdForUpdate(employee.getId()))
                .thenReturn(Optional.of(employee));
        when(serviceRepository.findById(service.getId()))
                .thenReturn(Optional.of(service));

        InvalidBookingException exception = assertThrows(
                InvalidBookingException.class,
                () -> bookingService.create(customer.getEmail(), request)
        );

        assertEquals("Booking must start on valid time slot", exception.getMessage());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void shouldAllowOwnerToCancelBooking() {
        User user = TestDataFactory.customer();

        Booking booking = TestDataFactory.bookingBuilder()
                .withUser(user)
                .build();

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.cancel(
                booking.getId(),
                user.getEmail()
        );

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository).save(captor.capture());

        Booking savedBooking = captor.getValue();

        assertEquals(BookingStatus.CANCELLED, savedBooking.getStatus());
    }

    @Test
    void shouldAllowAdminToCancelBooking() {
        User admin = TestDataFactory.admin();
        User user = TestDataFactory.customer();

        Booking booking = TestDataFactory.bookingBuilder()
                .withUser(user)
                .build();

        when(userRepository.findByEmail(admin.getEmail()))
                .thenReturn(Optional.of(admin));

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.cancel(
                booking.getId(),
                admin.getEmail()
        );

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository).save(captor.capture());

        Booking savedBooking = captor.getValue();

        assertEquals(BookingStatus.CANCELLED, savedBooking.getStatus());
    }

    @Test
    void shouldRejectCancellationByDifferentCustomer() {
        User user = TestDataFactory.customer();
        User otherUser = TestDataFactory
                .userBuilder()
                .withId(2L)
                .withEmail("otherUser@example.com")
                .build();

        Booking booking = TestDataFactory
                .bookingBuilder()
                .withUser(user)
                .build();

        BookingStatus originalStatus = booking.getStatus();

        when(userRepository.findByEmail(otherUser.getEmail()))
                .thenReturn(Optional.of(otherUser));

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(
                AccessDeniedException.class,
                () -> bookingService.cancel(
                        booking.getId(),
                        otherUser.getEmail()
                )
        );

        assertEquals(originalStatus, booking.getStatus());

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBookingDoesNotExistDuringCancellation() {
        User user = TestDataFactory.customer();

        Long bookingId = 999L;

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.cancel(
                        bookingId,
                        user.getEmail()
                )
        );

        assertEquals("Booking", exception.getResource());
        assertEquals("id", exception.getMatcher());
        assertEquals(bookingId, exception.getValue());

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }
}
