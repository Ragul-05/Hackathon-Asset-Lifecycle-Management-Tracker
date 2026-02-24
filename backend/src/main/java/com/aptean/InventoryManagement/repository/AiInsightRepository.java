package com.aptean.InventoryManagement.repository;

import com.aptean.InventoryManagement.model.AiInsight;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiInsightRepository extends JpaRepository<AiInsight, UUID> {
    List<AiInsight> findByUseCaseIgnoreCaseOrderByGeneratedAtDesc(String useCase);
    List<AiInsight> findAllByOrderByGeneratedAtDesc();
    List<AiInsight> findByUseCaseIgnoreCaseAndAssetIsNotNullOrderByGeneratedAtDesc(String useCase);
    List<AiInsight> findByUseCaseIgnoreCaseAndAsset_IdOrderByGeneratedAtDesc(String useCase, UUID assetId);
}
