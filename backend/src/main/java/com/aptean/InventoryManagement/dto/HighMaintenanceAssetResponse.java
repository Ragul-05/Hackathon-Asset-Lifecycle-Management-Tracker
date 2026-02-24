package com.aptean.InventoryManagement.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record HighMaintenanceAssetResponse(
        UUID assetId,
        String assetName,
        BigDecimal totalCost
) {}
