package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.AiRecommendationItem;
import com.aptean.InventoryManagement.dto.InventoryReportItem;
import com.aptean.InventoryManagement.dto.MaintenanceCostReportItem;
import com.aptean.InventoryManagement.dto.DepreciationSummaryResponse;
import com.aptean.InventoryManagement.model.AiInsight;
import com.aptean.InventoryManagement.model.Asset;
import com.aptean.InventoryManagement.repository.AiInsightRepository;
import com.aptean.InventoryManagement.repository.AssetRepository;
import com.aptean.InventoryManagement.repository.MaintenanceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final AssetRepository assetRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final DepreciationService depreciationService;
    private final AiInsightRepository aiInsightRepository;

    public ReportService(AssetRepository assetRepository,
                         MaintenanceRepository maintenanceRepository,
                         DepreciationService depreciationService,
                         AiInsightRepository aiInsightRepository) {
        this.assetRepository = assetRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.depreciationService = depreciationService;
        this.aiInsightRepository = aiInsightRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryReportItem> inventory() {
        return assetRepository.findAll().stream()
                .map(a -> new InventoryReportItem(
                        a.getId(),
                        a.getName(),
                        a.getCategory(),
                        a.getStatus(),
                        a.getPurchaseCost(),
                        a.getPurchaseDate(),
                        a.getLocation()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceCostReportItem> maintenanceCosts() {
        return maintenanceRepository.totalCostPerAsset().stream()
                .map(o -> new MaintenanceCostReportItem((UUID) o[0], (String) o[1], (BigDecimal) o[2]))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepreciationSummaryResponse> depreciationSummaries() {
        return assetRepository.findWithLifeAndPurchaseDate().stream()
                .filter(a -> a.getUsefulLifeMonths() != null && a.getUsefulLifeMonths() > 0)
                .filter(a -> a.getPurchaseCost() != null && a.getPurchaseDate() != null)
                .map(Asset::getId)
                .map(depreciationService::summarize)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AiRecommendationItem> aiRecommendations(String useCase) {
        Stream<AiInsight> stream = useCase != null && !useCase.isBlank()
                ? aiInsightRepository.findByUseCaseIgnoreCaseOrderByGeneratedAtDesc(useCase).stream()
                : aiInsightRepository.findAllByOrderByGeneratedAtDesc().stream();

        return stream
                .map(i -> new AiRecommendationItem(
                        i.getId(),
                        i.getUseCase(),
                        i.getAsset() != null ? i.getAsset().getId() : null,
                        i.getAsset() != null ? i.getAsset().getName() : null,
                        i.getResult(),
                        i.getGeneratedAt()
                ))
                .toList();
    }
}
