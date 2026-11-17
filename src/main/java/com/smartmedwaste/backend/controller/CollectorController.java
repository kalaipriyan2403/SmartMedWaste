package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.WasteItemResponse;
import com.smartmedwaste.backend.entity.*;
import com.smartmedwaste.backend.entity.enums.EventType;
import com.smartmedwaste.backend.entity.enums.NotificationType;
import com.smartmedwaste.backend.entity.enums.RequestStatus;
import com.smartmedwaste.backend.entity.enums.UserRole;
import com.smartmedwaste.backend.entity.enums.WasteStatus;
import com.smartmedwaste.backend.repository.AppUserRepository;
import com.smartmedwaste.backend.repository.CollectionRequestRepository;
import com.smartmedwaste.backend.repository.NotificationRepository;
import com.smartmedwaste.backend.repository.TrackingEventRepository;
import com.smartmedwaste.backend.repository.WasteItemRepository;
import com.smartmedwaste.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collector")
@RequiredArgsConstructor
public class CollectorController {

    private final WasteItemRepository wasteItemRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final NotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;

    @PostMapping("/confirm-pickup/{trackingId}")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Transactional
    public ResponseEntity<?> confirmPickup(
            @PathVariable String trackingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        // 1. Find waste item using tracking ID
        WasteItem item = wasteItemRepository.findByTrackingId(trackingId).orElse(null);

        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. Check whether the item is ready for collection
        if (item.getStatus() != WasteStatus.READY_FOR_COLLECTION) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Item is not ready for collection (current status: "
                                    + item.getStatus() + ")"
                    ));
        }

        // 3. Get logged-in collector
        AppUser collector = principal.getUser();

        // 4. Find collection request
        CollectionRequest request =
                collectionRequestRepository
                        .findByWasteItemId(item.getId())
                        .orElse(null);

        // 5. Update collection request
        if (request != null) {
            request.setCollector(collector);
            request.setStatus(RequestStatus.COLLECTED);
            collectionRequestRepository.save(request);
        }

        // 6. Update waste item status
        item.setStatus(WasteStatus.COLLECTED_STORED);
        wasteItemRepository.save(item);

        // 7. Create tracking event
        trackingEventRepository.save(
                TrackingEvent.builder()
                        .wasteItem(item)
                        .actor(collector)
                        .eventType(EventType.COLLECTED)
                        .build()
        );

        // 8. Notify collector
        notificationRepository.save(
                com.smartmedwaste.backend.entity.Notification.builder()
                        .user(collector)
                        .wasteItem(item)
                        .notificationType(NotificationType.ACTIVITY)
                        .message(
                                "You collected "
                                        + trackingId
                                        + " and moved it to storage."
                        )
                        .build()
        );

        // 9. Notify drivers in the same organization
        Long organizationId = wasteItemRepository
                .findOrganizationIdByWasteItemId(item.getId())
                .orElse(null);

        if (organizationId != null) {
            appUserRepository.findByOrganizationIdAndRole(
                    organizationId,
                    UserRole.DRIVER
            ).forEach(driver -> notificationRepository.save(
                    Notification.builder()
                            .user(driver)
                            .wasteItem(item)
                            .notificationType(NotificationType.ACTIVITY)
                            .message("Waste ready for transit: " + trackingId)
                            .build()
            ));
        }

        // 10. Return successful response to Android
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Pickup confirmed, moved to on-site storage",

                        "trackingId",
                        trackingId,

                        "status",
                        item.getStatus()
                )
        );
    }
}
