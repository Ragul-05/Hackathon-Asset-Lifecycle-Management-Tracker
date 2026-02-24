package com.aptean.InventoryManagement.repository;

import com.aptean.InventoryManagement.model.Asset;
import com.aptean.InventoryManagement.model.AssetStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {
    boolean existsBySerialNumberIgnoreCase(String serialNumber);
    Optional<Asset> findBySerialNumberIgnoreCase(String serialNumber);
    List<Asset> findByStatus(AssetStatus status);

    @Query("select a.category, count(a) from Asset a group by a.category")
    List<Object[]> countByCategory();

    @Query("select a.status, count(a) from Asset a group by a.status")
    List<Object[]> countByStatus();

    @Query("select a from Asset a where a.usefulLifeMonths is not null and a.purchaseDate is not null")
    List<Asset> findWithLifeAndPurchaseDate();
}
