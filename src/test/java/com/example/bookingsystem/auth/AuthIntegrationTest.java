package com.example.bookingsystem.auth;

import com.example.bookingsystem.auth.dto.LoginRequest;
import com.example.bookingsystem.auth.dto.RegisterRequest;
import com.example.bookingsystem.support.PostgresIntegrationTest;
import com.example.bookingsystem.support.TestDataFactory;
import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRepository;
import com.example.bookingsystem.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@AutoConfigureMockMvc
class AuthIntegrationTest extends PostgresIntegrationTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "auth-register@example.com",
                PASSWORD,
                "Test User"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        User savedUser = userRepository
                .findByEmail(request.email())
                .orElseThrow();

        assertEquals(request.email(), savedUser.getEmail());
        assertEquals(request.name(), savedUser.getName());
        assertEquals(UserRole.CUSTOMER, savedUser.getRole());
    }

    @Test
    void shouldHashPasswordWhenRegisteringUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "auth-hash@example.com",
                PASSWORD,
                "Test User"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        User savedUser = userRepository
                .findByEmail(request.email())
                .orElseThrow();

        assertNotEquals(
                PASSWORD,
                savedUser.getPasswordHash()
        );

        assertTrue(
                passwordEncoder.matches(
                        PASSWORD,
                        savedUser.getPasswordHash()
                )
        );
    }

    @Test
    void shouldReturnConflictWhenRegisteringDuplicateEmail()
            throws Exception {

        RegisterRequest request = new RegisterRequest(
                "auth-duplicate@example.com",
                PASSWORD,
                "Test User"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLoginWhenCredentialsAreCorrect()
            throws Exception {

        User user = saveUser(
                "auth-login@example.com"
        );

        LoginRequest request = new LoginRequest(
                user.getEmail(),
                PASSWORD
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordIsIncorrect()
            throws Exception {

        User user = saveUser(
                "auth-wrong-password@example.com"
        );

        LoginRequest request = new LoginRequest(
                user.getEmail(),
                "wrong-password"
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenUserDoesNotExist()
            throws Exception {

        LoginRequest request = new LoginRequest(
                "auth-does-not-exist@example.com",
                PASSWORD
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAccessTokenWhenLoginSucceeds()
            throws Exception {

        User user = saveUser(
                "auth-token@example.com"
        );

        LoginRequest request = new LoginRequest(
                user.getEmail(),
                PASSWORD
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void shouldReturnAuthenticatedUserWhenLoginSucceeds()
            throws Exception {

        User user = saveUser(
                "auth-user-response@example.com"
        );

        LoginRequest request = new LoginRequest(
                user.getEmail(),
                PASSWORD
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.user.id")
                                .value(user.getId())
                )
                .andExpect(
                        jsonPath("$.user.email")
                                .value(user.getEmail())
                )
                .andExpect(
                        jsonPath("$.user.role")
                                .value(UserRole.CUSTOMER.name())
                );
    }

    @Test
    void shouldAccessProtectedEndpointWithValidJwt()
            throws Exception {

        User user = saveUser(
                "auth-protected@example.com"
        );

        String accessToken = loginAndGetToken(
                user.getEmail()
        );

        /*
         * The booking does not exist, so we expect 404.
         *
         * The important thing is that we DON'T get 401.
         * That proves the real Bearer JWT passed authentication
         * and the request reached the application.
         */
        mockMvc.perform(
                        get("/api/bookings/{id}", 999_999L)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/bookings/{id}", 999_999L)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/bookings/{id}", 999_999L)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer definitely-not-a-valid-jwt"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsExpired()
            throws Exception {

        User user = saveUser(
                "auth-expired@example.com"
        );

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("booking-system")
                .issuedAt(now.minusSeconds(3600))
                .expiresAt(now.minusSeconds(60))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .build();

        String expiredToken = jwtEncoder
                .encode(
                        JwtEncoderParameters.from(claims)
                )
                .getTokenValue();

        mockMvc.perform(
                        get("/api/bookings/{id}", 999_999L)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + expiredToken
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    private User saveUser(
            String email
    ) {
        return userRepository.save(
                TestDataFactory.userBuilder()
                        .withId(null)
                        .withEmail(email)
                        .withPasswordHash(
                                passwordEncoder.encode(AuthIntegrationTest.PASSWORD)
                        )
                        .withRole(UserRole.CUSTOMER)
                        .build()
        );
    }

    private String loginAndGetToken(
            String email
    ) throws Exception {

        LoginRequest request = new LoginRequest(
                email,
                AuthIntegrationTest.PASSWORD
        );

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andReturn();

        var response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return response
                .get("accessToken").asString();
    }
}
