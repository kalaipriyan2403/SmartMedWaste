package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.entity.WasteItem;
import com.smartmedwaste.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only endpoints used by the Admin dashboard (waste history, GPS map, disposal register).
 * Copy this file into: src/main/java/com/smartmedwaste/backend/controller/
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final WasteItemRepository wasteItemRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final VehicleRepository vehicleRepository;
    private final DisposalRecordRepository disposalRecordRepository;

    public record EventDto(Long id, String eventType, String actorName, String actorRole,
                           BigDecimal latitude, BigDecimal longitude, LocalDateTime timestamp) {}
    public record VehicleDto(Long id, String vehicleNo, String driverName,
                             BigDecimal latitude, BigDecimal longitude, LocalDateTime updatedAt) {}
    public record DisposalDto(Long id, String trackingId, String category, BigDecimal quantity, String unit,
                              String facilityOperator, LocalDateTime disposedAt, String remarks) {}

    @GetMapping("/tracking/{trackingId}/events")
    @Transactional(readOnly = true)
    public ResponseEntity<?> events(@PathVariable String trackingId) {
        WasteItem item = wasteItemRepository.findByTrackingId(trackingId).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();
        List<EventDto> result = trackingEventRepository.findByWasteItemIdOrderByTimestampAsc(item.getId())
                .stream()
                .map(e -> new EventDto(e.getId(), e.getEventType().name(), e.getActor().getName(),
                        e.getActor().getRole().name(), e.getLatitude(), e.getLongitude(), e.getTimestamp()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vehicles")
    @Transactional(readOnly = true)
    public List<VehicleDto> vehicles() {
        return vehicleRepository.findAll().stream()
                .map(v -> new VehicleDto(v.getId(), v.getVehicleNo(),
                        v.getDriver() != null ? v.getDriver().getName() : null,
                        v.getCurrentLat(), v.getCurrentLng(), v.getUpdatedAt()))
                .toList();
    }

    @GetMapping("/disposals")
    @Transactional(readOnly = true)
    public List<DisposalDto> disposals() {
        return disposalRecordRepository.findAll().stream().map(d -> {
            WasteItem w = d.getWasteItem();
            return new DisposalDto(d.getId(), w.getTrackingId(), w.getCategory().name(), w.getQuantity(),
                    w.getUnit().name(), d.getFacility() != null ? d.getFacility().getName() : null,
                    d.getDisposedAt(), d.getRemarks());
        }).toList();
    }
}
