package com.aptean.InventoryManagement.dto;

import java.time.Instant;
import java.util.UUID;

public record AiInsightResponse(
        String useCase,
        UUID assetId,
        String result,
        Instant generatedAt
) {}
