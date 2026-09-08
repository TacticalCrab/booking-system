package com.example.bookingsystem.support.builder;

import com.example.bookingsystem.user.User;
import com.example.bookingsystem.user.UserRole;
import org.springframework.test.util.ReflectionTestUtils;

public class UserTestBuilder {

    private String email = "test@example.com";
    private String passwordHash = "hashed-password";
    private String name = "Test User";
    private UserRole role = UserRole.CUSTOMER;
    private Long id = 1L;

    public UserTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public UserTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UserTestBuilder withRole(UserRole role) {
        this.role = role;
        return this;
    }

    public UserTestBuilder asAdmin() {
        this.role = UserRole.ADMIN;
        return this;
    }

    public UserTestBuilder asCustomer() {
        this.role = UserRole.CUSTOMER;
        return this;
    }

    public UserTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public UserTestBuilder withoutId() {
        this.id = null;
        return this;
    }

    public User build() {
        User user = new User(
                email,
                passwordHash,
                name,
                role
        );

        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }
}
