package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.AssetStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InventoryReportItem(
        UUID assetId,
        String name,
        String category,
        AssetStatus status,
        BigDecimal purchaseCost,
        LocalDate purchaseDate,
        String location
) {}
