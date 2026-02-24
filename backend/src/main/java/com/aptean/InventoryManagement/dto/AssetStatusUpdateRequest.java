package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.AssetStatus;
import jakarta.validation.constraints.NotNull;

public record AssetStatusUpdateRequest(@NotNull(message = "Status is required") AssetStatus status) {}
