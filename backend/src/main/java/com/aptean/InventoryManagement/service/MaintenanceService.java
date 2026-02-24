package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.MaintenanceCostResponse;
import com.aptean.InventoryManagement.dto.MaintenanceRequest;
import com.aptean.InventoryManagement.dto.MaintenanceResponse;
import com.aptean.InventoryManagement.model.Asset;
import com.aptean.InventoryManagement.model.Maintenance;
import com.aptean.InventoryManagement.model.MaintenanceStatus;
import com.aptean.InventoryManagement.repository.AssetRepository;
import com.aptean.InventoryManagement.repository.MaintenanceRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final AssetRepository assetRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, AssetRepository assetRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public MaintenanceResponse log(MaintenanceRequest request) {
        Asset asset = getAsset(request.assetId());
        Maintenance maintenance = mapToEntity(new Maintenance(), request, asset);
        return toResponse(maintenanceRepository.save(maintenance));
    }

    @Transactional
    public MaintenanceResponse update(UUID id, MaintenanceRequest request) {
        Maintenance maintenance = getMaintenance(id);
        Asset asset = getAsset(request.assetId());
        mapToEntity(maintenance, request, asset);
        return toResponse(maintenanceRepository.save(maintenance));
    }

    @Transactional
    public void delete(UUID id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance not found");
        }
        maintenanceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> search(UUID assetId, MaintenanceStatus status) {
        Specification<Maintenance> spec = buildSpec(assetId, status);
        return maintenanceRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceCostResponse totalCost(UUID assetId) {
        Asset asset = getAsset(assetId);
        BigDecimal total = maintenanceRepository.totalCostByAsset(asset.getId());
        return new MaintenanceCostResponse(asset.getId(), total);
    }

    private Specification<Maintenance> buildSpec(UUID assetId, MaintenanceStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (assetId != null) {
                predicates.add(cb.equal(root.get("asset").get("id"), assetId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Maintenance mapToEntity(Maintenance maintenance, MaintenanceRequest request, Asset asset) {
        maintenance.setAsset(asset);
        maintenance.setType(request.type());
        maintenance.setStatus(request.status() != null ? request.status() : MaintenanceStatus.SCHEDULED);
        maintenance.setScheduledFor(request.scheduledFor());
        maintenance.setCompletedOn(request.completedOn());
        maintenance.setCost(request.cost());
        maintenance.setVendor(request.vendor());
        maintenance.setNotes(request.notes());
        return maintenance;
    }

    private Maintenance getMaintenance(UUID id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance not found"));
    }

    private Asset getAsset(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset not found"));
    }

    private MaintenanceResponse toResponse(Maintenance maintenance) {
        return new MaintenanceResponse(
                maintenance.getId(),
                maintenance.getAsset().getId(),
                maintenance.getAsset().getName(),
                maintenance.getAsset().getSerialNumber(),
                maintenance.getType(),
                maintenance.getStatus(),
                maintenance.getScheduledFor(),
                maintenance.getCompletedOn(),
                maintenance.getCost(),
                maintenance.getVendor(),
                maintenance.getNotes(),
                maintenance.getCreatedAt(),
                maintenance.getUpdatedAt()
        );
    }
}
