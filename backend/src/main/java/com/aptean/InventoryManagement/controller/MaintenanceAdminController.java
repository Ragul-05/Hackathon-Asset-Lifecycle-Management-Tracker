package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.MaintenanceCostResponse;
import com.aptean.InventoryManagement.dto.MaintenanceRequest;
import com.aptean.InventoryManagement.dto.MaintenanceResponse;
import com.aptean.InventoryManagement.model.MaintenanceStatus;
import com.aptean.InventoryManagement.service.MaintenanceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/maintenance")
@PreAuthorize("hasRole('ADMIN')")
public class MaintenanceAdminController {

    private final MaintenanceService maintenanceService;

    public MaintenanceAdminController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceResponse> create(@Valid @RequestBody MaintenanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.log(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceResponse> update(@PathVariable UUID id,
                                                      @Valid @RequestBody MaintenanceRequest request) {
        return ResponseEntity.ok(maintenanceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        maintenanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceResponse>> search(
            @RequestParam(value = "assetId", required = false) UUID assetId,
            @RequestParam(value = "status", required = false) MaintenanceStatus status
    ) {
        return ResponseEntity.ok(maintenanceService.search(assetId, status));
    }

    @GetMapping("/cost")
    public ResponseEntity<MaintenanceCostResponse> cost(@RequestParam UUID assetId) {
        return ResponseEntity.ok(maintenanceService.totalCost(assetId));
    }
}
