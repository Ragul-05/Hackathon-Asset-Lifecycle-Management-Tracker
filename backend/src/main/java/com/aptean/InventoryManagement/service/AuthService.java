package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.AuthRequest;
import com.aptean.InventoryManagement.dto.AuthResponse;
import com.aptean.InventoryManagement.dto.RegisterRequest;
import com.aptean.InventoryManagement.model.Department;
import com.aptean.InventoryManagement.model.User;
import com.aptean.InventoryManagement.repository.DepartmentRepository;
import com.aptean.InventoryManagement.repository.UserRepository;
import com.aptean.InventoryManagement.security.JwtService;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(true)
            .department(resolveDepartment(request.departmentId()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        Instant expiresAt = Instant.now().plusSeconds(jwtService.getExpirationSeconds());
        return new AuthResponse(token, expiresAt, user.getRole(), user.getFullName(), user.getEmail());
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String token = jwtService.generateToken(user);
        Instant expiresAt = Instant.now().plusSeconds(jwtService.getExpirationSeconds());
        return new AuthResponse(token, expiresAt, user.getRole(), user.getFullName(), user.getEmail());
    }

    private Department resolveDepartment(java.util.UUID id) {
        if (id == null) {
            return null;
        }
        return departmentRepository.findById(id).orElse(null);
    }
}
