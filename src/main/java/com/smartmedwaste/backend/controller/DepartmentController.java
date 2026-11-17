package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.DepartmentRequest;
import com.smartmedwaste.backend.dto.DepartmentResponse;
import com.smartmedwaste.backend.entity.Department;
import com.smartmedwaste.backend.entity.Organization;
import com.smartmedwaste.backend.repository.DepartmentRepository;
import com.smartmedwaste.backend.repository.OrganizationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;

    private DepartmentResponse toResponse(Department dept) {
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .orgId(dept.getOrganization().getId()) // safe — just reads the FK, no lazy load needed
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody DepartmentRequest request) {
        Organization org = organizationRepository.findById(request.getOrgId()).orElse(null);
        if (org == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid organization ID"));
        }

        Department dept = Department.builder()
                .name(request.getName())
                .organization(org)
                .build();

        Department saved = departmentRepository.save(dept);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        List<DepartmentResponse> result = departmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/org/{orgId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentResponse>> getByOrg(@PathVariable Long orgId) {
        List<DepartmentResponse> result = departmentRepository.findByOrganizationId(orgId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        Department dept = departmentRepository.findById(id).orElse(null);
        if (dept == null) {
            return ResponseEntity.notFound().build();
        }
        dept.setName(request.getName());
        Department updated = departmentRepository.save(dept);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        departmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}