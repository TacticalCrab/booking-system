package com.example.bookingsystem.support;

import com.example.bookingsystem.auth.refresh.RefreshTokenRepository;
import com.example.bookingsystem.booking.BookingRepository;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class PostgresIntegrationTest {

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

    protected void cleanDatabase() {
        bookingRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        employeeRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();
    }
}
