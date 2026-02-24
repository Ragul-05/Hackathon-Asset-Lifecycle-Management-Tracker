package com.aptean.InventoryManagement.dto;

import java.time.Instant;
import java.util.UUID;

public record AiRecommendationItem(
        UUID id,
        String useCase,
        UUID assetId,
        String assetName,
        String result,
        Instant generatedAt
) {}
