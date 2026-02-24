package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull(message = "Role is required") Role role) {}
