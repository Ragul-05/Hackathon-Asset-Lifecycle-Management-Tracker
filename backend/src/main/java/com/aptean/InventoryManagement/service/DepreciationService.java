package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.DepreciationEntryResponse;
import com.aptean.InventoryManagement.dto.DepreciationSummaryResponse;
import com.aptean.InventoryManagement.model.Asset;
import com.aptean.InventoryManagement.model.DepreciationMethod;
import com.aptean.InventoryManagement.repository.AssetRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DepreciationService {

    private final AssetRepository assetRepository;

    public DepreciationService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional(readOnly = true)
    public DepreciationSummaryResponse summarize(UUID assetId) {
        Asset asset = getAsset(assetId);
        validateFields(asset);
        BigDecimal cost = asset.getPurchaseCost();
        BigDecimal salvage = asset.getSalvageValue() != null ? asset.getSalvageValue() : BigDecimal.ZERO;
        int lifeMonths = asset.getUsefulLifeMonths();

        BigDecimal monthly = cost.subtract(salvage)
                .divide(BigDecimal.valueOf(lifeMonths), 2, RoundingMode.HALF_UP);

        LocalDate start = asset.getPurchaseDate();
        LocalDate today = LocalDate.now();
        long elapsedMonths = Math.max(0, ChronoUnit.MONTHS.between(start.withDayOfMonth(1), today.withDayOfMonth(1)));
        elapsedMonths = Math.min(elapsedMonths, lifeMonths);

        BigDecimal accumulated = monthly.multiply(BigDecimal.valueOf(elapsedMonths)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentValue = cost.subtract(accumulated).max(salvage).setScale(2, RoundingMode.HALF_UP);

        List<DepreciationEntryResponse> schedule = buildSchedule(start, lifeMonths, monthly, cost, salvage);

        return new DepreciationSummaryResponse(
                asset.getId(),
                asset.getName(),
                DepreciationMethod.STRAIGHT_LINE,
                cost,
                salvage,
                lifeMonths,
                accumulated,
                currentValue,
                schedule
        );
    }

    private List<DepreciationEntryResponse> buildSchedule(LocalDate start, int lifeMonths, BigDecimal monthly, BigDecimal cost, BigDecimal salvage) {
        List<DepreciationEntryResponse> list = new ArrayList<>();
        BigDecimal book = cost;
        for (int i = 0; i < lifeMonths; i++) {
            LocalDate periodStart = start.plusMonths(i).withDayOfMonth(1);
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
            book = book.subtract(monthly).max(salvage).setScale(2, RoundingMode.HALF_UP);
            list.add(new DepreciationEntryResponse(periodStart, periodEnd, monthly, book));
        }
        return list;
    }

    private Asset getAsset(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset not found"));
    }

    private void validateFields(Asset asset) {
        if (asset.getPurchaseCost() == null || asset.getPurchaseDate() == null || asset.getUsefulLifeMonths() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset missing purchase cost/date/useful life");
        }
        if (asset.getUsefulLifeMonths() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Useful life must be positive");
        }
    }
}
