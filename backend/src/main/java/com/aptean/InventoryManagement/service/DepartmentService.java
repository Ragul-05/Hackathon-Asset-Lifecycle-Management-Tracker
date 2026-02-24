package com.aptean.InventoryManagement.service;

import com.aptean.InventoryManagement.dto.DepartmentRequest;
import com.aptean.InventoryManagement.dto.DepartmentResponse;
import com.aptean.InventoryManagement.model.Department;
import com.aptean.InventoryManagement.repository.DepartmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        validateUniqueness(request.name(), request.code(), null);
        Department department = new Department();
        department.setName(request.name().trim());
        department.setCode(request.code().trim().toUpperCase());
        department.setDescription(request.description());
        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Transactional
    public DepartmentResponse update(UUID id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        validateUniqueness(request.name(), request.code(), id);

        department.setName(request.name().trim());
        department.setCode(request.code().trim().toUpperCase());
        department.setDescription(request.description());
        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
        }
        departmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }

    private void validateUniqueness(String name, String code, UUID currentId) {
        departmentRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already exists");
            }
        });

        departmentRepository.findByCodeIgnoreCase(code).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Department code already exists");
            }
        });
    }
}
