package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.OrganizationRequest;
import com.smartmedwaste.backend.entity.Organization;
import com.smartmedwaste.backend.repository.OrganizationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Organization> create(@Valid @RequestBody OrganizationRequest request) {
        Organization org = Organization.builder()
                .name(request.getName())
                .address(request.getAddress())
                .build();
        Organization saved = organizationRepository.save(org);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Organization>> getAll() {
        return ResponseEntity.ok(organizationRepository.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody OrganizationRequest request) {
        Organization org = organizationRepository.findById(id).orElse(null);
        if (org == null) return ResponseEntity.notFound().build();
        org.setName(request.getName());
        org.setAddress(request.getAddress());
        return ResponseEntity.ok(organizationRepository.save(org));
    }
}