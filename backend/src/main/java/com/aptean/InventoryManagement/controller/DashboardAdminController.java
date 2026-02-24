package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.DashboardResponse;
import com.aptean.InventoryManagement.service.DashboardService;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardAdminController {

    private final DashboardService dashboardService;

    public DashboardAdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> summary(
            @RequestParam(name = "highMaintenanceThreshold", defaultValue = "1000") BigDecimal highMaintenanceThreshold,
            @RequestParam(name = "maintenanceWindowDays", defaultValue = "30") int maintenanceWindowDays) {
        return ResponseEntity.ok(dashboardService.summary(highMaintenanceThreshold, maintenanceWindowDays));
    }
}
