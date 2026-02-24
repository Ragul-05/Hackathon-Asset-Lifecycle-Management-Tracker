package com.aptean.InventoryManagement.dto;

import java.util.UUID;

public record UsefulLifeAssetResponse(
        UUID assetId,
        String assetName,
        int remainingMonths,
        int totalLifeMonths
) {}
