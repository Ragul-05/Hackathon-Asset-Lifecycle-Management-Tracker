package com.aptean.InventoryManagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") String password
) {}
