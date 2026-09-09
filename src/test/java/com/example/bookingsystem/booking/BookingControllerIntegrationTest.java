package com.example.bookingsystem.booking;

import com.example.bookingsystem.booking.dto.CreateBookingRequest;
import com.example.bookingsystem.employee.Employee;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.employee.workinghours.EmployeeWorkingHours;
import com.example.bookingsystem.service.ServiceEntity;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.support.PostgresIntegrationTest;
import com.example.bookingsystem.support.TestDataFactory;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import com.example.bookingsystem.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.bookingsystem.support.TestDateTimeFactory.future;
import static com.example.bookingsystem.support.TestDateTimeFactory.past;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@AutoConfigureMockMvc
class BookingControllerIntegrationTest
        extends PostgresIntegrationTest {

    private static final Long NON_EXISTENT_ID = 999_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User customer;
    private ServiceEntity service;
    private Employee employee;
    private UserDetails customerPrincipal;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        customer = userRepository.save(
                TestDataFactory.userBuilder()
                        .withId(null)
                        .withEmail("customer@example.com")
                        .withPasswordHash("unused")
                        .withRole(UserRole.CUSTOMER)
                        .build()
        );

        service = serviceRepository.save(
                TestDataFactory.serviceBuilder()
                        .withId(null)
                        .build()
        );

        employee = saveEmployee(
                "employee@example.com",
                List.of(service),
                TestDataFactory.workingHoursAllWeekWithoutIds()
        );

        customerPrincipal = principalFor(customer);
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingBookingWithoutAuthentication()
            throws Exception {

        CreateBookingRequest request = bookingRequest(
                future(DayOfWeek.MONDAY, 10, 0)
        );

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateBookingWhenRequestIsValid()
            throws Exception {

        CreateBookingRequest request = bookingRequest(
                future(DayOfWeek.MONDAY, 10, 0)
        );

        createBooking(request)
                .andExpect(status().isCreated());

        assertEquals(1, bookingRepository.count());

        Booking booking = bookingRepository
                .findAll()
                .getFirst();

        assertEquals(customer.getId(), booking.getUser().getId());
        assertEquals(employee.getId(), booking.getEmployee().getId());
        assertEquals(service.getId(), booking.getService().getId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void shouldReturnBadRequestWhenBookingStartTimeIsInPast()
            throws Exception {

        CreateBookingRequest request = bookingRequest(
                past(DayOfWeek.MONDAY, 10, 0)
        );

        createBooking(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenServiceDoesNotExist()
            throws Exception {

        CreateBookingRequest request = bookingRequest(
                employee.getId(),
                NON_EXISTENT_ID,
                future(DayOfWeek.MONDAY, 10, 0)
        );

        createBooking(request)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeDoesNotExist()
            throws Exception {

        CreateBookingRequest request = bookingRequest(
                NON_EXISTENT_ID,
                service.getId(),
                future(DayOfWeek.MONDAY, 10, 0)
        );

        createBooking(request)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenBookingOverlaps()
            throws Exception {

        LocalDateTime existingStart =
                future(DayOfWeek.MONDAY, 10, 0);

        saveBooking(existingStart);

        CreateBookingRequest request = bookingRequest(
                existingStart.plusMinutes(10)
        );

        createBooking(request)
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenEmployeeDoesNotProvideService()
            throws Exception {

        ServiceEntity notProvidedService = serviceRepository.save(
                TestDataFactory.serviceBuilder()
                        .withId(null)
                        .build()
        );

        CreateBookingRequest request = bookingRequest(
                employee.getId(),
                notProvidedService.getId(),
                future(DayOfWeek.MONDAY, 10, 0)
        );

        createBooking(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBookingIsOutsideWorkingHours()
            throws Exception {

        EmployeeWorkingHours mondayWorkingHours =
                TestDataFactory.workingHoursBuilder()
                        .withId(null)
                        .withDayOfWeek(DayOfWeek.MONDAY)
                        .withStartTime(LocalTime.of(10, 0))
                        .withEndTime(LocalTime.of(15, 0))
                        .build();

        Employee limitedEmployee = saveEmployee(
                "limited-employee@example.com",
                List.of(service),
                List.of(mondayWorkingHours)
        );

        CreateBookingRequest request = bookingRequest(
                limitedEmployee.getId(),
                service.getId(),
                future(DayOfWeek.MONDAY, 16, 0)
        );

        createBooking(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCancelOwnBooking()
            throws Exception {

        Booking booking = saveBooking(
                future(DayOfWeek.MONDAY, 10, 0)
        );

        cancelBooking(
                booking.getId(),
                customerPrincipal
        )
        .andExpect(status().isOk());

        Booking cancelledBooking = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertEquals(
                BookingStatus.CANCELLED,
                cancelledBooking.getStatus()
        );
    }

    @Test
    void shouldReturnForbiddenWhenCancellingAnotherUsersBooking() throws Exception {
        Booking booking = saveBooking(
                future(DayOfWeek.MONDAY, 10, 0)
        );

        User anotherUser = saveAnotherUser();

        UserDetails principal = principalFor(anotherUser);

        cancelBooking(
                booking.getId(),
                principal
        )
        .andExpect(status().isForbidden());

        Booking unchangedBooking = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertEquals(
                BookingStatus.CONFIRMED,
                unchangedBooking.getStatus()
        );
    }

    @Test
    void shouldAllowAdminToCancelAnyBooking() throws Exception {
        Booking booking = saveBooking(
                future(DayOfWeek.MONDAY, 10, 0)
        );

        User adminUser = saveAdmin();

        UserDetails principal = principalFor(adminUser);

        cancelBooking(
                booking.getId(),
                principal
        )
        .andExpect(status().isOk());

        Booking cancelledBooking = bookingRepository
                .findById(booking.getId())
                .orElseThrow();

        assertEquals(
                BookingStatus.CANCELLED,
                cancelledBooking.getStatus()
        );
    }

    @Test
    void shouldReturnNotFoundWhenCancellingNonexistentBooking()
            throws Exception {

        cancelBooking(
                NON_EXISTENT_ID,
                customerPrincipal
        ).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBookingWhenOwnerRequestsIt() throws Exception {
        LocalDateTime existingStart =
                future(DayOfWeek.MONDAY, 10, 0);

        Booking existingBooking = saveBooking(existingStart);

        getBooking(
                existingBooking.getId(),
                customerPrincipal
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingBooking.getId()))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.name()));
    }

    @Test
    void shouldReturnBookingWhenAdminRequestsIt() throws Exception {
        User admin = saveAdmin();
        LocalDateTime existingStart =
                future(DayOfWeek.MONDAY, 10, 0);

        Booking existingBooking = saveBooking(existingStart);

        getBooking(
                existingBooking.getId(),
                principalFor(admin)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingBooking.getId()))
                .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.name()));
    }

    @Test
    void shouldReturnForbiddenWhenDifferentCustomerRequestsBooking() throws Exception {
        LocalDateTime existingStart =
                future(DayOfWeek.MONDAY, 10, 0);

        Booking existingBooking = saveBooking(existingStart);

        User anotherUser = saveAnotherUser();

        getBooking(
                existingBooking.getId(),
                principalFor(anotherUser)
        ).andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenGettingBookingWithoutAuthentication() throws Exception {
        LocalDateTime existingStart =
                future(DayOfWeek.MONDAY, 10, 0);

        Booking booking = saveBooking(existingStart);

        mockMvc.perform(
                get("/api/bookings/{id}", booking.getId())
        ).andExpect(status().isUnauthorized());
    }

    private ResultActions createBooking(
            CreateBookingRequest request
    ) throws Exception {

        return createBooking(
                request,
                customerPrincipal
        );
    }

    private ResultActions createBooking(
            CreateBookingRequest request,
            UserDetails principal
    ) throws Exception {

        return mockMvc.perform(
                post("/api/bookings")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );
    }

    private ResultActions cancelBooking(
            Long bookingId,
            UserDetails principal
    ) throws Exception {
        return mockMvc.perform(
                patch("/api/bookings/{id}/cancel", bookingId)
                        .with(user(principal))
        );
    }

    private ResultActions getBooking(
            Long bookingId,
            UserDetails principal
    ) throws Exception {
        return mockMvc.perform(
                get("/api/bookings/{id}", bookingId)
                        .with(user(principal))
        );
    }

    private CreateBookingRequest bookingRequest(
            LocalDateTime startTime
    ) {
        return bookingRequest(
                employee.getId(),
                service.getId(),
                startTime
        );
    }

    private CreateBookingRequest bookingRequest(
            Long employeeId,
            Long serviceId,
            LocalDateTime startTime
    ) {
        return new CreateBookingRequest(
                employeeId,
                serviceId,
                startTime
        );
    }

    private User saveAnotherUser() {
        return userRepository.save(
                TestDataFactory.userBuilder()
                        .withId(null)
                        .withEmail("customer2@example.com")
                        .withPasswordHash("unused")
                        .withRole(UserRole.CUSTOMER)
                        .build()
        );
    }

    private User saveAdmin() {
        return userRepository.save(
                TestDataFactory.userBuilder()
                        .withId(null)
                        .withEmail("admin@example.com")
                        .withPasswordHash("unused")
                        .withRole(UserRole.ADMIN)
                        .build()
        );
    }

    private Booking saveBooking(
            LocalDateTime startTime
    ) {
        Booking booking = TestDataFactory.bookingBuilder()
                .withId(null)
                .withUser(customer)
                .withEmployee(employee)
                .withService(service)
                .withStartTime(startTime)
                .build();

        return bookingRepository.saveAndFlush(booking);
    }

    private Employee saveEmployee(
            String email,
            List<ServiceEntity> services,
            List<EmployeeWorkingHours> workingHours
    ) {
        Employee employee = TestDataFactory.employeeBuilder()
                .withId(null)
                .withEmail(email)
                .withServices(new ArrayList<>(services))
                .withWorkingHours(new ArrayList<>(workingHours))
                .build();

        return employeeRepository.save(employee);
    }

    private UserDetails principalFor(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("unused")
                .roles(user.getRole().name())
                .build();
    }
}
