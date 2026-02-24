package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.AssignmentResponse;
import com.aptean.InventoryManagement.model.AssignmentStatus;
import com.aptean.InventoryManagement.model.Role;
import com.aptean.InventoryManagement.repository.UserRepository;
import com.aptean.InventoryManagement.service.AssetAssignmentService;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
public class AssetAssignmentController {

    private final AssetAssignmentService assignmentService;
    private final UserRepository userRepository;

    public AssetAssignmentController(AssetAssignmentService assignmentService, UserRepository userRepository) {
        this.assignmentService = assignmentService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> myAssignments(
            Principal principal,
            @RequestParam(value = "assetId", required = false) UUID assetId,
            @RequestParam(value = "status", required = false) AssignmentStatus status
    ) {
        UUID employeeId = null;
        if (principal instanceof Authentication auth && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.EMPLOYEE.name()))) {
            employeeId = userRepository.findByEmail(principal.getName())
                    .map(u -> u.getId())
                    .orElse(null);
        }
        return ResponseEntity.ok(assignmentService.search(assetId, employeeId, status));
    }
}