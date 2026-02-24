package com.aptean.InventoryManagement.controller;

import com.aptean.InventoryManagement.dto.EmployeeCreateRequest;
import com.aptean.InventoryManagement.dto.EmployeeResponse;
import com.aptean.InventoryManagement.dto.PasswordResetRequest;
import com.aptean.InventoryManagement.dto.RoleUpdateRequest;
import com.aptean.InventoryManagement.service.EmployeeAdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeAdminController {

    private final EmployeeAdminService employeeAdminService;

    public EmployeeAdminController(EmployeeAdminService employeeAdminService) {
        this.employeeAdminService = employeeAdminService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeAdminService.createEmployee(request));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<EmployeeResponse> updateRole(@PathVariable UUID id,
                                                        @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(employeeAdminService.updateRole(id, request));
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID id,
                                              @Valid @RequestBody PasswordResetRequest request) {
        employeeAdminService.resetPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> listEmployees() {
        return ResponseEntity.ok(employeeAdminService.listEmployees());
    }
}
