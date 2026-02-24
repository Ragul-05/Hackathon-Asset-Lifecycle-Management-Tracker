package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") String password,
        @NotNull(message = "Role is required") Role role,
        UUID departmentId
) {}
