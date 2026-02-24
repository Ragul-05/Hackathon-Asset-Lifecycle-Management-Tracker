package com.aptean.InventoryManagement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentRequest(
        @NotNull(message = "Asset ID is required") UUID assetId,
        @NotNull(message = "Employee ID is required") UUID employeeId,
        LocalDate dueBackAt,
        String notes
) {}
