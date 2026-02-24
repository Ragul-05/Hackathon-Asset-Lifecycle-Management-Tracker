package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.AiInsightRequest;
import com.aptean.InventoryManagement.dto.AiInsightResponse;
import com.aptean.InventoryManagement.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
public class AiAdminController {

    private final AiService aiService;

    public AiAdminController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/insight")
    public ResponseEntity<AiInsightResponse> generateInsight(@Valid @RequestBody AiInsightRequest request) {
        return ResponseEntity.ok(aiService.generate(request));
    }
}