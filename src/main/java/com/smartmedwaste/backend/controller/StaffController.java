package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.AdminCreateStaffRequest;
import jakarta.validation.Valid;
import com.smartmedwaste.backend.dto.StaffResponse;
import com.smartmedwaste.backend.dto.StaffUpdateRequest;
import com.smartmedwaste.backend.entity.AppUser;
import com.smartmedwaste.backend.entity.Department;
import com.smartmedwaste.backend.entity.enums.UserStatus;
import com.smartmedwaste.backend.repository.AppUserRepository;
import com.smartmedwaste.backend.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final com.smartmedwaste.backend.service.AuthService authService;

    private StaffResponse toResponse(AppUser user) {
        return StaffResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .role(user.getRole())
                .orgId(user.getOrganization().getId())
                .deptId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .phone(user.getPhone())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffResponse>> getAll() {
        List<StaffResponse> result = appUserRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        AppUser user = appUserRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody StaffUpdateRequest request) {
        AppUser user = appUserRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getDeptId() != null) {
            Department dept = departmentRepository.findById(request.getDeptId()).orElse(null);
            if (dept == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid department ID"));
            }
            user.setDepartment(dept);
        }

        AppUser updated = appUserRepository.save(user);
        return ResponseEntity.ok(toResponse(updated));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        AppUser user = appUserRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setStatus(UserStatus.INACTIVE);
        appUserRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        AppUser user = appUserRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setStatus(UserStatus.ACTIVE);
        appUserRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody AdminCreateStaffRequest request) {
        try {
            StaffResponse response = authService.adminCreateStaff(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}