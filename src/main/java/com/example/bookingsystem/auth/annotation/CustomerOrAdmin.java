package com.example.bookingsystem.auth.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
        "hasAnyRole(" +
        "T(com.example.bookingsystem.user.UserRole).CUSTOMER.name()," +
        "T(com.example.bookingsystem.user.UserRole).ADMIN.name()" +
        ")"
)
public @interface CustomerOrAdmin {
}
