package com.example.bookingsystem.user;

import com.example.bookingsystem.auth.annotation.AdminOnly;
import com.example.bookingsystem.user.dto.CreateUserRequest;
import com.example.bookingsystem.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UserResponse getMe(
            @AuthenticationPrincipal UserDetails principal
    ) {
        String email = principal.getUsername();

        return service.getByEmail(email);
    }

    @GetMapping
    @AdminOnly
    public Page<UserResponse> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }


    @GetMapping("/{id}")
    @AdminOnly
    public UserResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @AdminOnly
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return service.create(request);
    }
}
