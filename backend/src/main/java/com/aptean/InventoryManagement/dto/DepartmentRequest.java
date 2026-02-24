package com.aptean.InventoryManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must be at most 120 characters")
        String name,

        @NotBlank(message = "Code is required")
        @Size(max = 40, message = "Code must be at most 40 characters")
        String code,

        @Size(max = 255, message = "Description must be at most 255 characters")
        String description
) {}
