package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.EmployeeMaintenanceRequest;
import com.aptean.InventoryManagement.dto.MaintenanceRequest;
import com.aptean.InventoryManagement.dto.MaintenanceResponse;
import com.aptean.InventoryManagement.model.MaintenanceStatus;
import com.aptean.InventoryManagement.service.AssetAssignmentService;
import com.aptean.InventoryManagement.service.MaintenanceService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/maintenance")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeMaintenanceController {

    private final MaintenanceService maintenanceService;
    private final AssetAssignmentService assignmentService;

    public EmployeeMaintenanceController(MaintenanceService maintenanceService, AssetAssignmentService assignmentService) {
        this.maintenanceService = maintenanceService;
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceResponse> requestMaintenance(@Valid @RequestBody EmployeeMaintenanceRequest request,
                                                                  Principal principal) {
        UUID employeeId = assignmentService.currentEmployeeId(principal);
        assignmentService.requireActiveAssignment(request.assetId(), employeeId);

        MaintenanceRequest internal = new MaintenanceRequest(
                request.assetId(),
                request.type(),
                MaintenanceStatus.SCHEDULED,
                request.scheduledFor(),
                null,
                null,
                null,
                request.notes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.log(internal));
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceResponse>> myRequests(@RequestParam UUID assetId,
                                                                @RequestParam(required = false) MaintenanceStatus status,
                                                                Principal principal) {
        UUID employeeId = assignmentService.currentEmployeeId(principal);
        assignmentService.requireActiveAssignment(assetId, employeeId);
        return ResponseEntity.ok(maintenanceService.search(assetId, status));
    }
}
