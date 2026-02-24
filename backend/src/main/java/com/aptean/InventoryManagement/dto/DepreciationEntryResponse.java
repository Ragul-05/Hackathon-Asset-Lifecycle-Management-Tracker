package com.aptean.InventoryManagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepreciationEntryResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal depreciationAmount,
        BigDecimal bookValueAfter
) {}
