package com.example.bookingsystem;

import com.example.bookingsystem.booking.BookingRepository;
import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.user.UserRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Phase2And3IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
        employeeRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectInvalidCreateRequests() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John Customer",
                                    "email": "not-an-email",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Haircut",
                                    "description": "Basic haircut",
                                    "durationMinutes": 30,
                                    "price": -1
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 1,
                                    "employeeId": 1,
                                    "serviceId": 1,
                                    "startTime": "2000-01-01T10:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnCentralizedNotFoundError() throws Exception {
        mockMvc.perform(get("/api/services/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Service with id 999999 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldPageAndSortServices() throws Exception {
        createService("Massage", 60, 150);
        createService("Haircut", 30, 80);

        mockMvc.perform(get("/api/services")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Haircut"));
    }

    @Test
    void shouldApplyFlywayMigrationsForCurrentSchema() throws Exception {
        Path database = Files.createTempFile("booking-phase-3-", ".db");
        String jdbcUrl = "jdbc:sqlite:" + database.toAbsolutePath();

        Flyway.configure()
                .dataSource(jdbcUrl, null, null)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            assertEquals(
                    Set.of("users", "services", "employees", "employee_services", "bookings"),
                    applicationTables(connection)
            );
            assertEquals(3, foreignKeyCount(connection, "bookings"));
            assertEquals(2, foreignKeyCount(connection, "employee_services"));
            assertTrue(indexExists(connection, "idx_bookings_user_id"));
            assertTrue(indexExists(connection, "idx_bookings_employee_id"));
            assertTrue(indexExists(connection, "idx_bookings_service_id"));
        }

        Files.deleteIfExists(database);
    }

    private void createService(String name, int durationMinutes, int price) throws Exception {
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "description": "%s description",
                                    "durationMinutes": %d,
                                    "price": %d
                                }
                                """.formatted(name, name, durationMinutes, price)))
                .andExpect(status().isCreated());
    }

    private Set<String> applicationTables(Connection connection) throws Exception {
        Set<String> tableNames = new HashSet<>();

        try (ResultSet resultSet = connection.createStatement().executeQuery("""
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                  AND name NOT LIKE 'sqlite_%'
                  AND name <> 'flyway_schema_history'
                """)) {
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("name"));
            }
        }

        return tableNames;
    }

    private int foreignKeyCount(Connection connection, String tableName) throws Exception {
        int count = 0;

        try (ResultSet resultSet = connection.createStatement()
                .executeQuery("PRAGMA foreign_key_list(" + tableName + ")")) {
            while (resultSet.next()) {
                count++;
            }
        }

        return count;
    }

    private boolean indexExists(Connection connection, String indexName) throws Exception {
        try (ResultSet resultSet = connection.createStatement().executeQuery("""
                SELECT 1
                FROM sqlite_master
                WHERE type = 'index'
                  AND name = '%s'
                """.formatted(indexName))) {
            return resultSet.next();
        }
    }
}
