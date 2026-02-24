package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.AssetStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        String name,
        String category,
        String serialNumber,
        LocalDate purchaseDate,
        BigDecimal purchaseCost,
        String vendor,
        AssetStatus status,
        String location,
        Integer usefulLifeMonths,
        BigDecimal salvageValue,
        String imageUrl,
        String documentUrl,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
