package com.example.bookingsystem.phase;

import com.example.bookingsystem.booking.BookingRepository;

import com.example.bookingsystem.employee.EmployeeRepository;
import com.example.bookingsystem.service.ServiceRepository;
import com.example.bookingsystem.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

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
    void shouldCreateCompleteBookingFlow() throws Exception {

        // 1. Create user
        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John",
                                    "email": "john@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode userJson = objectMapper.readTree(userResponse);
        long userId = userJson.get("id").asLong();


        // 2. Create service
        String serviceResponse = mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Haircut",
                                    "description": "Basic haircut",
                                    "durationMinutes": 30,
                                    "price": 80
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode serviceJson = objectMapper.readTree(serviceResponse);
        long serviceId = serviceJson.get("id").asLong();

        mockMvc.perform(get("/api/services/{id}", serviceId))
                .andExpect(status().isOk());


        // 3. Create employee
        String employeeResponse = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Bob",
                                    "email": "bob@example.com",
                                    "servicesIds": [%d]
                                }
                                """.formatted(serviceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode employeeJson = objectMapper.readTree(employeeResponse);
        long employeeId = employeeJson.get("id").asLong();


        // 4. Create booking
        String bookingResponse = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "employeeId": %d,
                                    "serviceId": %d,
                                    "startTime": "2026-09-10T10:00:00",
                                    "endTime": "2026-09-10T10:30:00",
                                    "status": "CONFIRMED"
                                }
                                """.formatted(
                                userId,
                                employeeId,
                                serviceId
                        )))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode bookingJson = objectMapper.readTree(bookingResponse);
        long bookingId = bookingJson.get("id").asLong();


        // 5. Fetch booking through HTTP
        mockMvc.perform(get("/api/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.user.id").value(userId))
                .andExpect(jsonPath("$.employee.id").value(employeeId))
                .andExpect(jsonPath("$.service.id").value(serviceId));


        // 6. Verify that it REALLY reached the database
        assertTrue(bookingRepository.existsById(bookingId));
    }
}