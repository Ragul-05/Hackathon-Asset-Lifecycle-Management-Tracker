package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.AssignmentRequest;
import com.aptean.InventoryManagement.dto.AssignmentResponse;
import com.aptean.InventoryManagement.model.AssignmentStatus;
import com.aptean.InventoryManagement.service.AssetAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/assignments")
@PreAuthorize("hasRole('ADMIN')")
public class AssetAssignmentAdminController {

    private final AssetAssignmentService assignmentService;

    public AssetAssignmentAdminController(AssetAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<AssignmentResponse> assign(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.assign(request));
    }

    @PutMapping("/{id}/reassign")
    public ResponseEntity<AssignmentResponse> reassign(@PathVariable UUID id,
                                                       @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.reassign(id, request));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<AssignmentResponse> markReturned(@PathVariable UUID id) {
        return ResponseEntity.ok(assignmentService.markReturned(id));
    }

    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> search(
            @RequestParam(value = "assetId", required = false) UUID assetId,
            @RequestParam(value = "employeeId", required = false) UUID employeeId,
            @RequestParam(value = "status", required = false) AssignmentStatus status
    ) {
        return ResponseEntity.ok(assignmentService.search(assetId, employeeId, status));
    }
}
