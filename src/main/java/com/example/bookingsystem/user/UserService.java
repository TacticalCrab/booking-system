package com.example.bookingsystem.user;

import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.user.dto.CreateUserRequest;
import com.example.bookingsystem.user.dto.UserResponse;
import com.example.bookingsystem.user.exception.UserAlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        repository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponse> getAll(Pageable pageable) {
        return repository
                .findAll(pageable)
                .map(UserMapper::toResponse);
    }

    public UserResponse getById(Long id) {
        User user = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        return UserMapper.toResponse(user);
    }

    public UserResponse getByEmail(String email) {
        User user = repository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User", "email", email));

        return UserMapper.toResponse(user);
    }

    public UserResponse create(CreateUserRequest request) {
        if (repository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        UserRole role = Optional.ofNullable(request.role())
                .orElse(UserRole.CUSTOMER);

        User user = new User(
                request.email(),
                passwordHash,
                request.name(),
                role
        );

        User savedUser = repository.save(user);

        return UserMapper.toResponse(savedUser);
    }
}
