package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.MaintenanceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceResponse(
        UUID id,
        UUID assetId,
        String assetName,
        String serialNumber,
        String type,
        MaintenanceStatus status,
        LocalDate scheduledFor,
        LocalDate completedOn,
        BigDecimal cost,
        String vendor,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
