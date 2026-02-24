package com.aptean.InventoryManagement.repository;

import com.aptean.InventoryManagement.model.Maintenance;
import com.aptean.InventoryManagement.model.MaintenanceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID>, JpaSpecificationExecutor<Maintenance> {
    List<Maintenance> findByAssetId(UUID assetId);
    Optional<Maintenance> findFirstByAssetIdAndStatus(UUID assetId, MaintenanceStatus status);

    @Query("select coalesce(sum(m.cost),0) from Maintenance m where m.asset.id = :assetId")
    BigDecimal totalCostByAsset(@Param("assetId") UUID assetId);

    List<Maintenance> findByStatusAndScheduledForBetween(MaintenanceStatus status, LocalDate start, LocalDate end);

    @Query("select m.asset, coalesce(sum(m.cost),0) as total from Maintenance m group by m.asset having coalesce(sum(m.cost),0) > :threshold order by total desc")
    List<Object[]> findHighCostAssets(@Param("threshold") BigDecimal threshold);

    @Query("select m.asset.id, m.asset.name, coalesce(sum(m.cost),0) as total from Maintenance m group by m.asset.id, m.asset.name")
    List<Object[]> totalCostPerAsset();
}
