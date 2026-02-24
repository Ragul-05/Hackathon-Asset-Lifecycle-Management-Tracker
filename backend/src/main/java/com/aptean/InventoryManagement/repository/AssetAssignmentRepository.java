package com.aptean.InventoryManagement.repository;

import com.aptean.InventoryManagement.model.AssetAssignment;
import com.aptean.InventoryManagement.model.AssignmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, UUID>, JpaSpecificationExecutor<AssetAssignment> {
    Optional<AssetAssignment> findFirstByAssetIdAndStatus(UUID assetId, AssignmentStatus status);
    List<AssetAssignment> findByEmployeeId(UUID employeeId);
    Optional<AssetAssignment> findFirstByAssetIdAndEmployeeIdAndStatus(UUID assetId, UUID employeeId, AssignmentStatus status);
}
