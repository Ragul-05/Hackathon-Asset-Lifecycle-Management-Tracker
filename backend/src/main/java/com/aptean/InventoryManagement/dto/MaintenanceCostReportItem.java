package com.aptean.InventoryManagement.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MaintenanceCostReportItem(
        UUID assetId,
        String assetName,
        BigDecimal totalCost
) {}
