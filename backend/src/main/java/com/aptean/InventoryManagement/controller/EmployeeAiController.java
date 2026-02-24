package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.AiRecommendationItem;
import com.aptean.InventoryManagement.service.AiService;
import com.aptean.InventoryManagement.service.AssetAssignmentService;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/ai")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeAiController {

    private final AiService aiService;
    private final AssetAssignmentService assignmentService;

    public EmployeeAiController(AiService aiService, AssetAssignmentService assignmentService) {
        this.aiService = aiService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/insights")
    public ResponseEntity<List<AiRecommendationItem>> insights(@RequestParam UUID assetId,
                                                               @RequestParam(required = false) String useCase,
                                                               Principal principal) {
        UUID employeeId = assignmentService.currentEmployeeId(principal);
        assignmentService.requireActiveAssignment(assetId, employeeId);

        return ResponseEntity.ok(aiService.listInsights(useCase, assetId).stream()
                .map(i -> new AiRecommendationItem(
                        i.getId(),
                        i.getUseCase(),
                        i.getAsset() != null ? i.getAsset().getId() : null,
                        i.getAsset() != null ? i.getAsset().getName() : null,
                        i.getResult(),
                        i.getGeneratedAt()
                ))
                .toList());
    }
}
