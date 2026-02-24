package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.AssignmentStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        UUID assetId,
        String assetName,
        String serialNumber,
        UUID employeeId,
        String employeeName,
        AssignmentStatus status,
        Instant assignedAt,
        LocalDate dueBackAt,
        Instant returnedAt,
        String notes
) {}
