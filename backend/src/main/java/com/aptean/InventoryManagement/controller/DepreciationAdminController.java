package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.DepreciationSummaryResponse;
import com.aptean.InventoryManagement.service.DepreciationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/depreciation")
@PreAuthorize("hasRole('ADMIN')")
public class DepreciationAdminController {

    private final DepreciationService depreciationService;

    public DepreciationAdminController(DepreciationService depreciationService) {
        this.depreciationService = depreciationService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DepreciationSummaryResponse> summary(@RequestParam UUID assetId) {
        return ResponseEntity.ok(depreciationService.summarize(assetId));
    }
}
