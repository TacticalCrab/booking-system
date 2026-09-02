package com.example.bookingsystem.user.dto;

public record CreateUserRequest(
   String email,
   String password,
   String name
) {}
