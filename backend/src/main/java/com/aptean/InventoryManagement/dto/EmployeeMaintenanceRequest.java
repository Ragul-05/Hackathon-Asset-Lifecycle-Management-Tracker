package com.aptean.InventoryManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeMaintenanceRequest(
        @NotNull(message = "Asset ID is required") UUID assetId,
        @NotBlank(message = "Type is required") @Size(max = 120) String type,
        LocalDate scheduledFor,
        @Size(max = 512) String notes
) {}
