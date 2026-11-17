package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.entity.CollectionRequest;
import com.smartmedwaste.backend.entity.TrackingEvent;
import com.smartmedwaste.backend.entity.WasteItem;
import com.smartmedwaste.backend.entity.enums.EventType;
import com.smartmedwaste.backend.repository.CollectionRequestRepository;
import com.smartmedwaste.backend.repository.TrackingEventRepository;
import com.smartmedwaste.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoryController {

    private final TrackingEventRepository trackingEventRepository;
    private final CollectionRequestRepository collectionRequestRepository;

    public record HistoryDto(
            String trackingId,
            String eventType,
            LocalDateTime timestamp
    ) {}

    public record AttenderHistoryDto(
            String trackingId,
            String category,
            BigDecimal quantity,
            String unit,
            String status,
            LocalDateTime createdAt
    ) {}

    @GetMapping("/history/mine")
    @PreAuthorize("hasRole('COLLECTOR') or hasRole('DRIVER') or hasRole('DISPOSAL_OPERATOR')")
    @Transactional(readOnly = true)
    public List<HistoryDto> mine(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return trackingEventRepository.findAll().stream()
                .filter(e ->
                        e.getActor().getId().equals(principal.getId())
                                && e.getEventType() != EventType.GPS_PING
                )
                .sorted(
                        Comparator.comparing(
                                TrackingEvent::getTimestamp
                        ).reversed()
                )
                .map(e -> new HistoryDto(
                        e.getWasteItem().getTrackingId(),
                        e.getEventType().name(),
                        e.getTimestamp()
                ))
                .toList();
    }

    @GetMapping("/attender/history")
    @PreAuthorize("hasRole('ATTENDER')")
    @Transactional(readOnly = true)
    public List<AttenderHistoryDto> attenderHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return collectionRequestRepository
                .findByAttenderId(principal.getId())
                .stream()
                .sorted(
                        Comparator.comparing(
                                CollectionRequest::getCreatedAt
                        ).reversed()
                )
                .map(cr -> {
                    WasteItem w = cr.getWasteItem();

                    return new AttenderHistoryDto(
                            w.getTrackingId(),
                            w.getCategory().name(),
                            w.getQuantity(),
                            w.getUnit().name(),
                            w.getStatus().name(),
                            cr.getCreatedAt()
                    );
                })
                .toList();
    }
}