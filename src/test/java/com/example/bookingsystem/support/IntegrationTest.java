package com.example.bookingsystem.support;

import com.example.bookingsystem.auth.refresh.RefreshTokenRepository;
import com.example.bookingsystem.booking.BookingRepository;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

    @Autowired
    protected BookingRepository bookingRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected EmployeeRepository employeeRepository;

    @Autowired
    protected ServiceRepository serviceRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected void cleanDatabase() {
        // Delete dependent records before their referenced rows.
        jdbcTemplate.update("DELETE FROM outbox_events");
        jdbcTemplate.update("DELETE FROM idempotency_records");
        jdbcTemplate.update("DELETE FROM bookings");
        jdbcTemplate.update("DELETE FROM refresh_tokens");
        jdbcTemplate.update("DELETE FROM employee_working_hours");
        jdbcTemplate.update("DELETE FROM employee_services");
        jdbcTemplate.update("DELETE FROM employees");
        jdbcTemplate.update("DELETE FROM services");
        jdbcTemplate.update("DELETE FROM users");
    }
}
