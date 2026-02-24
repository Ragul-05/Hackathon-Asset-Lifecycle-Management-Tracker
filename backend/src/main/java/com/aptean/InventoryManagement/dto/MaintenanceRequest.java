package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceRequest(
        @NotNull(message = "Asset ID is required") UUID assetId,
        @NotBlank(message = "Type is required") @Size(max = 120) String type,
        MaintenanceStatus status,
        LocalDate scheduledFor,
        LocalDate completedOn,
        BigDecimal cost,
        @Size(max = 120) String vendor,
        @Size(max = 512) String notes
) {}
