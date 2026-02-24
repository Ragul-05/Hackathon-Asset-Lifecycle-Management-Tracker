package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 160)
        String name,

        @NotBlank(message = "Category is required")
        @Size(max = 80)
        String category,

        @NotBlank(message = "Serial number is required")
        @Size(max = 120)
        String serialNumber,

        LocalDate purchaseDate,

        BigDecimal purchaseCost,

        @Size(max = 120)
        String vendor,

        AssetStatus status,

        @Size(max = 160)
        String location,

        Integer usefulLifeMonths,

        BigDecimal salvageValue,

        @Size(max = 512)
        String notes
) {}
