package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.AssetRequest;
import com.aptean.InventoryManagement.dto.AssetResponse;
import com.aptean.InventoryManagement.dto.AssetStatusUpdateRequest;
import com.aptean.InventoryManagement.model.Asset;
import com.aptean.InventoryManagement.model.AssetStatus;
import com.aptean.InventoryManagement.repository.AssetRepository;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final CloudinaryService cloudinaryService;

    public AssetService(AssetRepository assetRepository, CloudinaryService cloudinaryService) {
        this.assetRepository = assetRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public AssetResponse create(AssetRequest request) {
        validateSerialUnique(request.serialNumber(), null);
        Asset asset = mapToEntity(new Asset(), request);
        Asset saved = assetRepository.save(asset);
        return toResponse(saved);
    }

    @Transactional
    public AssetResponse update(UUID id, AssetRequest request) {
        Asset asset = getAsset(id);
        validateSerialUnique(request.serialNumber(), id);
        Asset updated = mapToEntity(asset, request);
        return toResponse(assetRepository.save(updated));
    }

    @Transactional
    public void delete(UUID id) {
        Asset asset = getAsset(id);
        cloudinaryService.delete(asset.getImagePublicId());
        cloudinaryService.delete(asset.getDocumentPublicId());
        assetRepository.delete(asset);
    }

    @Transactional
    public AssetResponse updateStatus(UUID id, AssetStatusUpdateRequest request) {
        Asset asset = getAsset(id);
        asset.setStatus(request.status());
        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse uploadImage(UUID id, MultipartFile file) {
        Asset asset = getAsset(id);
        try {
            Map result = cloudinaryService.upload(file, "image");
            cloudinaryService.delete(asset.getImagePublicId());
            asset.setImageUrl((String) result.get("secure_url"));
            asset.setImagePublicId((String) result.get("public_id"));
            return toResponse(assetRepository.save(asset));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Image upload failed");
        }
    }

    @Transactional
    public AssetResponse uploadDocument(UUID id, MultipartFile file) {
        Asset asset = getAsset(id);
        try {
            Map result = cloudinaryService.upload(file, "auto");
            cloudinaryService.delete(asset.getDocumentPublicId());
            asset.setDocumentUrl((String) result.get("secure_url"));
            asset.setDocumentPublicId((String) result.get("public_id"));
            return toResponse(assetRepository.save(asset));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Document upload failed");
        }
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> search(String q, AssetStatus status, String category) {
        Specification<Asset> spec = buildSpec(q, status, category);
        return assetRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse get(UUID id) {
        return toResponse(getAsset(id));
    }

    private Specification<Asset> buildSpec(String q, AssetStatus status, String category) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%%" + q.toLowerCase() + "%%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("serialNumber")), like),
                        cb.like(cb.lower(root.get("category")), like)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Asset mapToEntity(Asset asset, AssetRequest request) {
        asset.setName(request.name());
        asset.setCategory(request.category());
        asset.setSerialNumber(request.serialNumber());
        asset.setPurchaseDate(request.purchaseDate());
        asset.setPurchaseCost(request.purchaseCost());
        asset.setVendor(request.vendor());
        asset.setStatus(request.status() != null ? request.status() : AssetStatus.AVAILABLE);
        asset.setLocation(request.location());
        asset.setUsefulLifeMonths(request.usefulLifeMonths());
        asset.setSalvageValue(request.salvageValue());
        asset.setNotes(request.notes());
        return asset;
    }

    private AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getCategory(),
                asset.getSerialNumber(),
                asset.getPurchaseDate(),
                asset.getPurchaseCost(),
                asset.getVendor(),
                asset.getStatus(),
                asset.getLocation(),
                asset.getUsefulLifeMonths(),
                asset.getSalvageValue(),
                asset.getImageUrl(),
                asset.getDocumentUrl(),
                asset.getNotes(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }

    private Asset getAsset(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }

    private void validateSerialUnique(String serialNumber, UUID currentId) {
        assetRepository.findBySerialNumberIgnoreCase(serialNumber).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Serial number already exists");
            }
        });
    }
}
