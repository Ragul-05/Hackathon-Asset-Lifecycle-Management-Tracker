package com.aptean.InventoryManagement.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

public record AiInsightRequest(
        @NotBlank(message = "Use case is required") String useCase,
        UUID assetId,
        Map<String, Object> context
) {}
