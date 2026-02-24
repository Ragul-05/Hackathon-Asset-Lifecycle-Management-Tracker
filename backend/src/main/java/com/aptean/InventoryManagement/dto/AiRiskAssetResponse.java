package com.aptean.InventoryManagement.dto;

import java.time.Instant;
import java.util.UUID;

public record AiRiskAssetResponse(
        UUID insightId,
        UUID assetId,
        String assetName,
        String result,
        Instant generatedAt
) {}
