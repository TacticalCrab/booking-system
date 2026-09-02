package com.example.bookingsystem.user;

import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.user.dto.CreateUserRequest;
import com.example.bookingsystem.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        repository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAll() {
        return repository
                .findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse getById(Long id) {
        User user = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        return UserMapper.toResponse(user);
    }

    public UserResponse create(CreateUserRequest request) {
        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                request.email(),
                passwordHash,
                request.name(),
                UserRole.CUSTOMER
        );

        User savedUser = repository.save(user);

        return UserMapper.toResponse(savedUser);
    }
}
