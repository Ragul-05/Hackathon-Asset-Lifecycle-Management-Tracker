package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.AiRiskAssetResponse;
import com.aptean.InventoryManagement.dto.DashboardResponse;
import com.aptean.InventoryManagement.dto.HighMaintenanceAssetResponse;
import com.aptean.InventoryManagement.dto.MaintenanceDueItem;
import com.aptean.InventoryManagement.dto.UsefulLifeAssetResponse;
import com.aptean.InventoryManagement.model.AiInsight;
import com.aptean.InventoryManagement.model.Asset;
import com.aptean.InventoryManagement.model.AssetStatus;
import com.aptean.InventoryManagement.model.MaintenanceStatus;
import com.aptean.InventoryManagement.repository.AiInsightRepository;
import com.aptean.InventoryManagement.repository.AssetRepository;
import com.aptean.InventoryManagement.repository.MaintenanceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final AssetRepository assetRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final AiInsightRepository aiInsightRepository;

    public DashboardService(AssetRepository assetRepository,
                            MaintenanceRepository maintenanceRepository,
                            AiInsightRepository aiInsightRepository) {
        this.assetRepository = assetRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.aiInsightRepository = aiInsightRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse summary(BigDecimal highMaintenanceThreshold, int maintenanceWindowDays) {
        BigDecimal threshold = highMaintenanceThreshold != null ? highMaintenanceThreshold : BigDecimal.ZERO;
        Map<String, Long> byCategory = assetRepository.countByCategory().stream()
                .collect(Collectors.toMap(o -> (String) o[0], o -> (Long) o[1]));

        Map<String, Long> byStatus = assetRepository.countByStatus().stream()
                .collect(Collectors.toMap(o -> ((AssetStatus) o[0]).name(), o -> (Long) o[1]));

        LocalDate today = LocalDate.now();
        LocalDate windowEnd = today.plusDays(maintenanceWindowDays);
        List<MaintenanceDueItem> due = maintenanceRepository
                .findByStatusAndScheduledForBetween(MaintenanceStatus.SCHEDULED, today, windowEnd)
                .stream()
                .map(m -> new MaintenanceDueItem(m.getId(), m.getAsset().getId(), m.getAsset().getName(), m.getType(), m.getScheduledFor()))
                .toList();

        List<HighMaintenanceAssetResponse> highMaintenance = maintenanceRepository.findHighCostAssets(threshold)
                .stream()
                .map(o -> {
                    Asset asset = (Asset) o[0];
                    BigDecimal total = (BigDecimal) o[1];
                    return new HighMaintenanceAssetResponse(asset.getId(), asset.getName(), total);
                })
                .toList();

        List<UsefulLifeAssetResponse> nearEndOfLife = assetRepository.findWithLifeAndPurchaseDate().stream()
                .map(this::mapUsefulLife)
                .filter(Objects::nonNull)
                .toList();

        List<AiRiskAssetResponse> aiRiskAssets = aiInsightRepository
                .findByUseCaseIgnoreCaseAndAssetIsNotNullOrderByGeneratedAtDesc("risk")
                .stream()
                .map(this::mapRisk)
                .toList();

        return new DashboardResponse(byCategory, byStatus, due, highMaintenance, nearEndOfLife, aiRiskAssets);
    }

    private UsefulLifeAssetResponse mapUsefulLife(Asset asset) {
        LocalDate purchaseDate = asset.getPurchaseDate();
        Integer lifeMonths = asset.getUsefulLifeMonths();
        if (purchaseDate == null || lifeMonths == null || lifeMonths <= 0) {
            return null;
        }
        long elapsed = Math.max(0, ChronoUnit.MONTHS.between(purchaseDate.withDayOfMonth(1), LocalDate.now().withDayOfMonth(1)));
        int remaining = (int) Math.max(0, lifeMonths - elapsed);
        int thresholdByPercent = (int) Math.ceil(lifeMonths * 0.15);
        int threshold = Math.min(thresholdByPercent, 3);
        if (threshold < 0) {
            threshold = 0;
        }
        if (remaining <= threshold) {
            return new UsefulLifeAssetResponse(asset.getId(), asset.getName(), remaining, lifeMonths);
        }
        return null;
    }

    private AiRiskAssetResponse mapRisk(AiInsight insight) {
        return new AiRiskAssetResponse(
                insight.getId(),
                insight.getAsset() != null ? insight.getAsset().getId() : null,
                insight.getAsset() != null ? insight.getAsset().getName() : null,
                insight.getResult(),
                insight.getGeneratedAt()
        );
    }
}
