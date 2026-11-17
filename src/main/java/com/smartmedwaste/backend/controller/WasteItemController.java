package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.WasteItemResponse;
import com.smartmedwaste.backend.entity.WasteItem;
import com.smartmedwaste.backend.entity.enums.WasteStatus;
import com.smartmedwaste.backend.repository.WasteItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/waste-items")
@RequiredArgsConstructor
public class WasteItemController {

    private final WasteItemRepository wasteItemRepository;

    private WasteItemResponse toResponse(WasteItem item) {
        return WasteItemResponse.builder()
                .id(item.getId())
                .deptId(item.getDepartment().getId())
                .category(item.getCategory())
                .colourCode(item.getColourCode())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .bagCount(item.getBagCount())
                .trackingId(item.getTrackingId())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ATTENDER') or hasRole('ADMIN')")
    public ResponseEntity<List<WasteItemResponse>> getAll(
            @RequestParam(required = false) WasteStatus status
    ) {
        List<WasteItem> items = status != null
                ? wasteItemRepository.findAll().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList())
                : wasteItemRepository.findAll();

        return ResponseEntity.ok(items.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ATTENDER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return wasteItemRepository.findById(id)
                .map(item -> ResponseEntity.ok(toResponse(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/track/{trackingId}")
    @PreAuthorize("hasRole('COLLECTOR') or hasRole('DRIVER') or hasRole('DISPOSAL_OPERATOR') or hasRole('ATTENDER') or hasRole('ADMIN')")
    public ResponseEntity<?> getByTrackingId(@PathVariable String trackingId) {
        return wasteItemRepository.findByTrackingId(trackingId)
                .map(item -> ResponseEntity.ok(toResponse(item)))
                .orElse(ResponseEntity.notFound().build());
    }
}