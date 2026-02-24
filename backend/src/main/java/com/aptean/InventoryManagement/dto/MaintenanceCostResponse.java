package com.aptean.InventoryManagement.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MaintenanceCostResponse(UUID assetId, BigDecimal totalCost) {}
