package com.aptean.InventoryManagement.dto;

import com.aptean.InventoryManagement.model.Role;
import java.time.Instant;

public record AuthResponse(String token, Instant expiresAt, Role role, String fullName, String email) {}
