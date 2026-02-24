package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.EmployeeCreateRequest;
import com.aptean.InventoryManagement.dto.EmployeeResponse;
import com.aptean.InventoryManagement.dto.PasswordResetRequest;
import com.aptean.InventoryManagement.dto.RoleUpdateRequest;
import com.aptean.InventoryManagement.model.Department;
import com.aptean.InventoryManagement.model.Role;
import com.aptean.InventoryManagement.model.User;
import com.aptean.InventoryManagement.repository.DepartmentRepository;
import com.aptean.InventoryManagement.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeAdminService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeAdminService(UserRepository userRepository,
                                DepartmentRepository departmentRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        Department department = resolveDepartment(request.departmentId());

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .department(department)
                .build();

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public EmployeeResponse updateRole(UUID userId, RoleUpdateRequest request) {
        User user = getUser(userId);
        user.setRole(request.role());
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void resetPassword(UUID userId, PasswordResetRequest request) {
        User user = getUser(userId);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> listEmployees() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE)
                .map(this::toResponse)
                .toList();
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Department resolveDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found"));
    }

    private EmployeeResponse toResponse(User user) {
        return new EmployeeResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getCreatedAt()
        );
    }
}
