package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.Role;
import java.time.Instant;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String fullName,
        String email,
        Role role,
        UUID departmentId,
        String departmentName,
        Instant createdAt
) {}
