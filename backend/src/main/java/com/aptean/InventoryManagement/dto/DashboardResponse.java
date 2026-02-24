package com.aptean.InventoryManagement.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        Map<String, Long> assetsByCategory,
        Map<String, Long> assetsByStatus,
        List<MaintenanceDueItem> maintenanceDue,
        List<HighMaintenanceAssetResponse> highMaintenanceAssets,
        List<UsefulLifeAssetResponse> nearEndOfLifeAssets,
        List<AiRiskAssetResponse> aiRiskAssets
) {}
