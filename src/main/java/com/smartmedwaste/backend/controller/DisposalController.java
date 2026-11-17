package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.DisposalRequest;
import com.smartmedwaste.backend.entity.AppUser;
import com.smartmedwaste.backend.entity.DisposalRecord;
import com.smartmedwaste.backend.entity.TrackingEvent;
import com.smartmedwaste.backend.entity.WasteItem;
import com.smartmedwaste.backend.entity.enums.EventType;
import com.smartmedwaste.backend.entity.enums.NotificationType;
import com.smartmedwaste.backend.entity.enums.WasteStatus;
import com.smartmedwaste.backend.repository.DisposalRecordRepository;
import com.smartmedwaste.backend.repository.NotificationRepository;
import com.smartmedwaste.backend.repository.TrackingEventRepository;
import com.smartmedwaste.backend.repository.WasteItemRepository;
import com.smartmedwaste.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/disposal")
@RequiredArgsConstructor
public class DisposalController {

    private final WasteItemRepository wasteItemRepository;
    private final DisposalRecordRepository disposalRecordRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final NotificationRepository notificationRepository;


    /**
     * Disposal Operator confirms that the waste has reached
     * the disposal facility.
     */
    @PostMapping("/confirm-receipt/{trackingId}")
    @PreAuthorize("hasRole('DISPOSAL_OPERATOR')")
    @Transactional
    public ResponseEntity<?> confirmReceipt(
            @PathVariable String trackingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        WasteItem item = wasteItemRepository
                .findByTrackingId(trackingId)
                .orElse(null);

        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        if (item.getStatus() != WasteStatus.IN_TRANSIT) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Item is not in transit (current status: "
                                    + item.getStatus() + ")"
                    ));
        }

        AppUser operator = principal.getUser();

        // Record delivery/receipt event
        trackingEventRepository.save(
                TrackingEvent.builder()
                        .wasteItem(item)
                        .actor(operator)
                        .eventType(EventType.DELIVERED)
                        .build()
        );

        // Notify disposal operator
        notificationRepository.save(
                com.smartmedwaste.backend.entity.Notification.builder()
                        .user(operator)
                        .wasteItem(item)
                        .notificationType(NotificationType.ACTIVITY)
                        .message(
                                "You confirmed receipt of "
                                        + trackingId
                                        + "."
                        )
                        .build()
        );

        /*
         * Status remains IN_TRANSIT.
         *
         * Final status becomes DISPOSED when
         * recordDisposal() is called.
         */
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Receipt confirmed — ready to record disposal",
                        "trackingId",
                        trackingId,
                        "status",
                        item.getStatus()
                )
        );
    }


    /**
     * Records final disposal and completes the waste lifecycle.
     */
    @PostMapping("/record-disposal/{trackingId}")
    @PreAuthorize("hasRole('DISPOSAL_OPERATOR')")
    @Transactional
    public ResponseEntity<?> recordDisposal(
            @PathVariable String trackingId,
            @RequestBody(required = false) DisposalRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        WasteItem item = wasteItemRepository
                .findByTrackingId(trackingId)
                .orElse(null);

        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        if (item.getStatus() != WasteStatus.IN_TRANSIT) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Item is not awaiting disposal (current status: "
                                    + item.getStatus() + ")"
                    ));
        }

        AppUser operator = principal.getUser();

        // Create disposal record
        DisposalRecord record = DisposalRecord.builder()
                .wasteItem(item)
                .facility(operator)
                .disposedAt(LocalDateTime.now())
                .remarks(
                        request != null
                                ? request.getRemarks()
                                : null
                )
                .build();

        disposalRecordRepository.save(record);

        // Update final waste status
        item.setStatus(WasteStatus.DISPOSED);
        wasteItemRepository.save(item);

        // Record disposal tracking event
        trackingEventRepository.save(
                TrackingEvent.builder()
                        .wasteItem(item)
                        .actor(operator)
                        .eventType(EventType.DISPOSED)
                        .build()
        );

        // Notify operator
        notificationRepository.save(
                com.smartmedwaste.backend.entity.Notification.builder()
                        .user(operator)
                        .wasteItem(item)
                        .notificationType(NotificationType.ACTIVITY)
                        .message(
                                "You recorded disposal for "
                                        + trackingId
                                        + "."
                        )
                        .build()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Waste lifecycle complete",
                        "trackingId",
                        trackingId,
                        "status",
                        item.getStatus()
                )
        );
    }
}