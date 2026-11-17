package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.GpsPingRequest;
import com.smartmedwaste.backend.dto.VehicleRequest;
import com.smartmedwaste.backend.entity.AppUser;
import com.smartmedwaste.backend.entity.TrackingEvent;
import com.smartmedwaste.backend.entity.Vehicle;
import com.smartmedwaste.backend.entity.WasteItem;
import com.smartmedwaste.backend.entity.enums.EventType;
import com.smartmedwaste.backend.entity.enums.NotificationType;
import com.smartmedwaste.backend.entity.enums.UserRole;
import com.smartmedwaste.backend.entity.enums.WasteStatus;
import com.smartmedwaste.backend.repository.*;
import com.smartmedwaste.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final WasteItemRepository wasteItemRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;


    /**
     * Registers or updates the logged-in driver's vehicle.
     * Call once at first login.
     */
    @PostMapping("/vehicle")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> registerVehicle(
            @Valid @RequestBody VehicleRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        AppUser driver = principal.getUser();

        Vehicle vehicle = vehicleRepository.findByDriverId(driver.getId())
                .orElse(Vehicle.builder()
                        .driver(driver)
                        .build());

        vehicle.setVehicleNo(request.getVehicleNo());

        Vehicle saved = vehicleRepository.save(vehicle);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "vehicleId", saved.getId(),
                        "vehicleNo", saved.getVehicleNo()
                ));
    }


    @PostMapping("/start-transit/{trackingId}")
    @PreAuthorize("hasRole('DRIVER')")
    @Transactional
    public ResponseEntity<?> startTransit(
            @PathVariable String trackingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        WasteItem item = wasteItemRepository
                .findByTrackingId(trackingId)
                .orElse(null);

        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        if (item.getStatus() != WasteStatus.COLLECTED_STORED) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Item is not in storage yet (current status: "
                                    + item.getStatus() + ")"
                    ));
        }

        item.setStatus(WasteStatus.IN_TRANSIT);
        wasteItemRepository.save(item);

        trackingEventRepository.save(
                TrackingEvent.builder()
                        .wasteItem(item)
                        .actor(principal.getUser())
                        .eventType(EventType.PICKED_UP)
                        .build()
        );

        notificationRepository.save(
                com.smartmedwaste.backend.entity.Notification.builder()
                        .user(principal.getUser())
                        .wasteItem(item)
                        .notificationType(NotificationType.ACTIVITY)
                        .message("You started transit for " + trackingId + ".")
                        .build()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Transit started",
                        "trackingId", trackingId,
                        "status", item.getStatus()
                )
        );
    }


    /**
     * Called periodically by the app while a delivery is in progress.
     * Example: every 30 seconds.
     */
    @PostMapping("/gps-ping")
    @PreAuthorize("hasRole('DRIVER')")
    @Transactional
    public ResponseEntity<?> gpsPing(
            @Valid @RequestBody GpsPingRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        WasteItem item = wasteItemRepository
                .findByTrackingId(request.getTrackingId())
                .orElse(null);

        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        if (item.getStatus() != WasteStatus.IN_TRANSIT) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Item is not currently in transit"
                    ));
        }

        AppUser driver = principal.getUser();

        trackingEventRepository.save(
                TrackingEvent.builder()
                        .wasteItem(item)
                        .actor(driver)
                        .eventType(EventType.GPS_PING)
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .build()
        );

        vehicleRepository.findByDriverId(driver.getId())
                .ifPresent(vehicle -> {

                    vehicle.setCurrentLat(request.getLatitude());
                    vehicle.setCurrentLng(request.getLongitude());

                    vehicleRepository.save(vehicle);
                });

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Location updated"
                )
        );
    }


    @PostMapping("/complete-delivery/{trackingId}")
    @PreAuthorize("hasRole('DRIVER')")
    @Transactional
    public ResponseEntity<?> completeDelivery(
            @PathVariable String trackingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        // 1. Find waste item
        WasteItem item = wasteItemRepository
                .findByTrackingId(trackingId)
                .orElse(null);

        // 2. Check item before doing anything else
        if (item == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Verify status
        if (item.getStatus() != WasteStatus.IN_TRANSIT) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Item is not in transit (current status: "
                                    + item.getStatus() + ")"
                    ));
        }

        AppUser driver = principal.getUser();

        // 4. Create DELIVERED tracking event
        trackingEventRepository.save(
                TrackingEvent.builder()
                        .wasteItem(item)
                        .actor(driver)
                        .eventType(EventType.DELIVERED)
                        .build()
        );

        // 5. Notify driver
        notificationRepository.save(
                com.smartmedwaste.backend.entity.Notification.builder()
                        .user(driver)
                        .wasteItem(item)
                        .notificationType(NotificationType.ACTIVITY)
                        .message(
                                "You completed delivery for "
                                        + trackingId
                                        + "."
                        )
                        .build()
        );

        /*
         * 6. Find disposal operators.
         *
         * @Transactional keeps the Hibernate session open here.
         *
         * Therefore:
         *
         * item
         *   -> department
         *       -> organization
         *           -> id
         *
         * can be safely accessed.
         */
        appUserRepository
                .findByOrganizationIdAndRole(
                        item.getDepartment()
                                .getOrganization()
                                .getId(),
                        UserRole.DISPOSAL_OPERATOR
                )
                .forEach(op ->

                        notificationRepository.save(
                                com.smartmedwaste.backend.entity.Notification.builder()
                                        .user(op)
                                        .wasteItem(item)
                                        .notificationType(NotificationType.ACTIVITY)
                                        .message(
                                                "Incoming delivery: "
                                                        + trackingId
                                        )
                                        .build()
                        )
                );

        /*
         * Status intentionally remains IN_TRANSIT.
         *
         * Disposal Operator will finally confirm receipt
         * and update the waste status.
         */

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Delivery marked complete, awaiting facility confirmation",
                        "trackingId",
                        trackingId,
                        "status",
                        item.getStatus()
                )
        );
    }
}