package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.DepreciationMethod;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DepreciationSummaryResponse(
        UUID assetId,
        String assetName,
        DepreciationMethod method,
        BigDecimal purchaseCost,
        BigDecimal salvageValue,
        Integer usefulLifeMonths,
        BigDecimal accumulatedDepreciation,
        BigDecimal currentBookValue,
        List<DepreciationEntryResponse> schedule
) {}
