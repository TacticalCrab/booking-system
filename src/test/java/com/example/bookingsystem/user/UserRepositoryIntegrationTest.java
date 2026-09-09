package com.example.bookingsystem.user;

import com.example.bookingsystem.support.PostgresIntegrationTest;
import com.example.bookingsystem.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRepositoryIntegrationTest
        extends PostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        cleanDatabase();
    }

    @Test
    void shouldRejectDuplicateEmail() {
        User firstUser = TestDataFactory.userBuilder()
                .withId(null)
                .withEmail("duplicate@example.com")
                .build();

        User secondUser = TestDataFactory.userBuilder()
                .withId(null)
                .withEmail("duplicate@example.com")
                .build();

        userRepository.saveAndFlush(firstUser);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(secondUser)
        );
    }
}
