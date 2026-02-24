package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.AssetResponse;
import com.aptean.InventoryManagement.model.AssetStatus;
import com.aptean.InventoryManagement.service.AssetService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<AssetResponse>> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) AssetStatus status,
            @RequestParam(value = "category", required = false) String category
    ) {
        return ResponseEntity.ok(assetService.search(q, status, category));
    }
}
