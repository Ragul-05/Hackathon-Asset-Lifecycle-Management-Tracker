package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.AssignmentRequest;
import com.aptean.InventoryManagement.dto.AssignmentResponse;
import com.aptean.InventoryManagement.model.Asset;
import com.aptean.InventoryManagement.model.AssetAssignment;
import com.aptean.InventoryManagement.model.AssetStatus;
import com.aptean.InventoryManagement.model.AssignmentStatus;
import com.aptean.InventoryManagement.model.User;
import com.aptean.InventoryManagement.repository.AssetAssignmentRepository;
import com.aptean.InventoryManagement.repository.AssetRepository;
import com.aptean.InventoryManagement.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.security.Principal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetAssignmentService {

    private final AssetAssignmentRepository assignmentRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public AssetAssignmentService(AssetAssignmentRepository assignmentRepository,
                                  AssetRepository assetRepository,
                                  UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AssignmentResponse assign(AssignmentRequest request) {
        Asset asset = getAsset(request.assetId());
        if (hasActiveAssignment(asset.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset already assigned");
        }
        User employee = getEmployee(request.employeeId());

        AssetAssignment assignment = new AssetAssignment();
        assignment.setAsset(asset);
        assignment.setEmployee(employee);
        assignment.setDueBackAt(request.dueBackAt());
        assignment.setNotes(request.notes());
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignment.setAssignedAt(Instant.now());

        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);

        return toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponse reassign(UUID assignmentId, AssignmentRequest request) {
        AssetAssignment current = getAssignment(assignmentId);
        if (current.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active assignments can be reassigned");
        }
        closeAssignment(current);

        // reuse assign logic but enforce same asset or new asset? request carries assetId; allow changing asset too
        return assign(request);
    }

    @Transactional
    public AssignmentResponse markReturned(UUID assignmentId) {
        AssetAssignment assignment = getAssignment(assignmentId);
        if (assignment.getStatus() == AssignmentStatus.RETURNED) {
            return toResponse(assignment);
        }
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnedAt(Instant.now());
        assignmentRepository.save(assignment);

        Asset asset = assignment.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> search(UUID assetId, UUID employeeId, AssignmentStatus status) {
        Specification<AssetAssignment> spec = buildSpec(assetId, employeeId, status);
        return assignmentRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public void requireActiveAssignment(UUID assetId, UUID employeeId) {
        boolean owned = assignmentRepository
                .findFirstByAssetIdAndEmployeeIdAndStatus(assetId, employeeId, AssignmentStatus.ASSIGNED)
                .isPresent();
        if (!owned) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Asset not assigned to user");
        }
    }

    @Transactional(readOnly = true)
    public UUID currentEmployeeId(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Specification<AssetAssignment> buildSpec(UUID assetId, UUID employeeId, AssignmentStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (assetId != null) {
                predicates.add(cb.equal(root.get("asset").get("id"), assetId));
            }
            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean hasActiveAssignment(UUID assetId) {
        return assignmentRepository.findFirstByAssetIdAndStatus(assetId, AssignmentStatus.ASSIGNED).isPresent();
    }

    private void closeAssignment(AssetAssignment assignment) {
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnedAt(Instant.now());
        assignmentRepository.save(assignment);

        Asset asset = assignment.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);
    }

    private AssetAssignment getAssignment(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
    }

    private Asset getAsset(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset not found"));
    }

    private User getEmployee(UUID id) {
        return userRepository.findById(id)
                .filter(u -> u.getRole() == com.aptean.InventoryManagement.model.Role.EMPLOYEE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee not found"));
    }

    private AssignmentResponse toResponse(AssetAssignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getAsset().getId(),
                assignment.getAsset().getName(),
                assignment.getAsset().getSerialNumber(),
                assignment.getEmployee().getId(),
                assignment.getEmployee().getFullName(),
                assignment.getStatus(),
                assignment.getAssignedAt(),
                assignment.getDueBackAt(),
                assignment.getReturnedAt(),
                assignment.getNotes()
        );
    }
}
