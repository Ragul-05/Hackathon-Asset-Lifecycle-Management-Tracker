package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.AssetRequest;
import com.aptean.InventoryManagement.dto.AssetResponse;
import com.aptean.InventoryManagement.dto.AssetStatusUpdateRequest;
import com.aptean.InventoryManagement.model.AssetStatus;
import com.aptean.InventoryManagement.service.AssetService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/assets")
@PreAuthorize("hasRole('ADMIN')")
public class AssetAdminController {

    private final AssetService assetService;

    public AssetAdminController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponse> update(@PathVariable UUID id, @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.ok(assetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AssetResponse> updateStatus(@PathVariable UUID id,
                                                      @Valid @RequestBody AssetStatusUpdateRequest request) {
        return ResponseEntity.ok(assetService.updateStatus(id, request));
    }

    @PostMapping(path = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<AssetResponse> uploadImage(@PathVariable UUID id,
                                                     @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(assetService.uploadImage(id, file));
    }

    @PostMapping(path = "/{id}/document", consumes = "multipart/form-data")
    public ResponseEntity<AssetResponse> uploadDocument(@PathVariable UUID id,
                                                        @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(assetService.uploadDocument(id, file));
    }
}
