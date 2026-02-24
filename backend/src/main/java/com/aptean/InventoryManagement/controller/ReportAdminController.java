package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.AiRecommendationItem;
import com.aptean.InventoryManagement.dto.DepreciationSummaryResponse;
import com.aptean.InventoryManagement.dto.InventoryReportItem;
import com.aptean.InventoryManagement.dto.MaintenanceCostReportItem;
import com.aptean.InventoryManagement.service.ReportService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportAdminController {

    private final ReportService reportService;

    public ReportAdminController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryReportItem>> inventory() {
        return ResponseEntity.ok(reportService.inventory());
    }

    @GetMapping("/maintenance-cost")
    public ResponseEntity<List<MaintenanceCostReportItem>> maintenanceCosts() {
        return ResponseEntity.ok(reportService.maintenanceCosts());
    }

    @GetMapping("/depreciation")
    public ResponseEntity<List<DepreciationSummaryResponse>> depreciation() {
        return ResponseEntity.ok(reportService.depreciationSummaries());
    }

    @GetMapping("/ai-recommendations")
    public ResponseEntity<List<AiRecommendationItem>> aiRecommendations(@RequestParam(required = false) String useCase) {
        return ResponseEntity.ok(reportService.aiRecommendations(useCase));
    }
}
