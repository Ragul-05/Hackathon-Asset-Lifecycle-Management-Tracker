package com.aptean.InventoryManagement.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceDueItem(
        UUID maintenanceId,
        UUID assetId,
        String assetName,
        String type,
        LocalDate scheduledFor
) {}
