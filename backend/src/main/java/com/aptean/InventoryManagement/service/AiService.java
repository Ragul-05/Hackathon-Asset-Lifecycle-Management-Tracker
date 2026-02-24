package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.AiInsightRequest;
import com.aptean.InventoryManagement.dto.AiInsightResponse;
import com.aptean.InventoryManagement.model.AiInsight;
import com.aptean.InventoryManagement.model.Asset;
import com.aptean.InventoryManagement.repository.AiInsightRepository;
import com.aptean.InventoryManagement.repository.AssetRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiService {

    private final AssetRepository assetRepository;
    private final AiInsightRepository aiInsightRepository;
    private final RestTemplate restTemplate;
    private final String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s";

    public AiService(AssetRepository assetRepository, AiInsightRepository aiInsightRepository, @Value("${gemini.api-key:}") String apiKey) {
        this.assetRepository = assetRepository;
        this.aiInsightRepository = aiInsightRepository;
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    public AiInsightResponse generate(AiInsightRequest request) {
        Asset asset = null;
        if (request.assetId() != null) {
            asset = assetRepository.findById(request.assetId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset not found"));
        }

        String prompt = buildPrompt(request, asset);

        // If no API key, return a stubbed response to avoid runtime failure
        if (apiKey == null || apiKey.isBlank()) {
            String fallback = "[Gemini not configured] " + prompt;
            return persistAndRespond(request, asset, fallback);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> content = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(content, headers);
        String url = GEMINI_URL.formatted(apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            String text = extractText(response.getBody());
            return persistAndRespond(request, asset, text);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini request failed");
        }
    }

    private AiInsightResponse persistAndRespond(AiInsightRequest request, Asset asset, String text) {
        AiInsight insight = new AiInsight();
        insight.setUseCase(request.useCase());
        insight.setAsset(asset);
        insight.setResult(text);
        AiInsight saved = aiInsightRepository.save(insight);
        return new AiInsightResponse(saved.getUseCase(), saved.getAsset() != null ? saved.getAsset().getId() : null, saved.getResult(), saved.getGeneratedAt());
    }

    public java.util.List<AiInsight> listInsights(String useCase, UUID assetId) {
        if (assetId != null && useCase != null && !useCase.isBlank()) {
            return aiInsightRepository.findByUseCaseIgnoreCaseAndAsset_IdOrderByGeneratedAtDesc(useCase, assetId);
        }
        if (assetId != null) {
            return aiInsightRepository.findAllByOrderByGeneratedAtDesc().stream()
                    .filter(i -> i.getAsset() != null && i.getAsset().getId().equals(assetId))
                    .toList();
        }
        if (useCase != null && !useCase.isBlank()) {
            return aiInsightRepository.findByUseCaseIgnoreCaseOrderByGeneratedAtDesc(useCase);
        }
        return aiInsightRepository.findAllByOrderByGeneratedAtDesc();
    }

    private String buildPrompt(AiInsightRequest request, Asset asset) {
        StringBuilder sb = new StringBuilder();
        sb.append("Use case: ").append(request.useCase()).append("\n");
        if (asset != null) {
            sb.append("Asset: ").append(asset.getName())
                    .append(" | Serial: ").append(asset.getSerialNumber())
                    .append(" | Category: ").append(asset.getCategory())
                    .append(" | Purchase date: ").append(asset.getPurchaseDate())
                    .append(" | Cost: ").append(asset.getPurchaseCost())
                    .append(" | Status: ").append(asset.getStatus())
                    .append("\n");
        }
        if (request.context() != null && !request.context().isEmpty()) {
            sb.append("Context: ").append(request.context()).append("\n");
        }
        sb.append("Provide concise actionable insight.");
        return sb.toString();
    }

    private String extractText(Map body) {
        if (body == null) return "No response";
        try {
            var contents = (java.util.List<?>) body.get("candidates");
            if (contents == null || contents.isEmpty()) return "No response";
            var first = (Map) contents.get(0);
            var content = (Map) first.get("content");
            var parts = (java.util.List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "No response";
            var part = (Map) parts.get(0);
            Object text = part.get("text");
            return text != null ? text.toString() : "No response";
        } catch (Exception e) {
            return "No response";
        }
    }
}
