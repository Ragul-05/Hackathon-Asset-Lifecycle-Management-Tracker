package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.AssetResponse;
import com.aptean.InventoryManagement.dto.AssignmentResponse;
import com.aptean.InventoryManagement.dto.DepreciationSummaryResponse;
import com.aptean.InventoryManagement.dto.MaintenanceResponse;
import com.aptean.InventoryManagement.service.AssetAssignmentService;
import com.aptean.InventoryManagement.service.AssetService;
import com.aptean.InventoryManagement.service.DepreciationService;
import com.aptean.InventoryManagement.service.MaintenanceService;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/assets")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeAssetController {

    private final AssetAssignmentService assignmentService;
    private final AssetService assetService;
    private final DepreciationService depreciationService;
    private final MaintenanceService maintenanceService;

    public EmployeeAssetController(AssetAssignmentService assignmentService,
                                   AssetService assetService,
                                   DepreciationService depreciationService,
                                   MaintenanceService maintenanceService) {
        this.assignmentService = assignmentService;
        this.assetService = assetService;
        this.depreciationService = depreciationService;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<AssignmentResponse>> myAssignments(Principal principal) {
        UUID employeeId = currentUserId(principal);
        return ResponseEntity.ok(assignmentService.search(null, employeeId, null));
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<AssetResponse> assetDetails(@PathVariable UUID assetId, Principal principal) {
        ensureOwnership(assetId, principal);
        return ResponseEntity.ok(assetService.get(assetId));
    }

    @GetMapping("/{assetId}/depreciation")
    public ResponseEntity<DepreciationSummaryResponse> depreciation(@PathVariable UUID assetId, Principal principal) {
        ensureOwnership(assetId, principal);
        return ResponseEntity.ok(depreciationService.summarize(assetId));
    }

    @GetMapping("/{assetId}/history/assignments")
    public ResponseEntity<List<AssignmentResponse>> assignmentHistory(@PathVariable UUID assetId, Principal principal) {
        ensureOwnership(assetId, principal);
        return ResponseEntity.ok(assignmentService.search(assetId, null, null));
    }

    @GetMapping("/{assetId}/history/maintenance")
    public ResponseEntity<List<MaintenanceResponse>> maintenanceHistory(@PathVariable UUID assetId, Principal principal) {
        ensureOwnership(assetId, principal);
        return ResponseEntity.ok(maintenanceService.search(assetId, null));
    }

    private void ensureOwnership(UUID assetId, Principal principal) {
        UUID employeeId = currentUserId(principal);
        assignmentService.requireActiveAssignment(assetId, employeeId);
    }

    private UUID currentUserId(Principal principal) {
        return assignmentService.currentEmployeeId(principal);
    }
}
