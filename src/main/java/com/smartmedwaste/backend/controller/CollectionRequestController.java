package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.CollectionRequestResponse;
import com.smartmedwaste.backend.dto.EodRegistrationRequest;
import com.smartmedwaste.backend.entity.AppUser;
import com.smartmedwaste.backend.entity.CollectionRequest;
import com.smartmedwaste.backend.entity.Department;
import com.smartmedwaste.backend.entity.WasteItem;
import com.smartmedwaste.backend.entity.enums.RequestStatus;
import com.smartmedwaste.backend.entity.enums.WasteStatus;
import com.smartmedwaste.backend.repository.*;
import com.smartmedwaste.backend.security.UserPrincipal;
import com.smartmedwaste.backend.util.GovtColourMapper;
import com.smartmedwaste.backend.util.TrackingIdGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/collection-requests")
@RequiredArgsConstructor
public class CollectionRequestController {

    private final WasteItemRepository wasteItemRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final DepartmentRepository departmentRepository;
    private final TrackingIdGenerator trackingIdGenerator;
    private final NotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;


    /**
     * The Attender's EOD registration — the ONLY place a WasteItem actually
     * gets persisted. Dept Staff never creates a record; they only use the
     * AI suggestion tool to pick the right bin. The Attender fills this form
     * fresh, from what they physically packed into colour-coded bags.
     */
    @PostMapping("/register-eod")
    @PreAuthorize("hasRole('ATTENDER')")
    public ResponseEntity<?> registerEod(
            @Valid @RequestBody EodRegistrationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Department dept = departmentRepository.findById(request.getDeptId()).orElse(null);
        if (dept == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid department ID"));
        }

        // Save first (without trackingId) to get a real, unique DB-assigned ID
        WasteItem item = WasteItem.builder()
                .department(dept)
                .category(request.getCategory())
                .colourCode(GovtColourMapper.colourFor(request.getCategory()))
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .bagCount(request.getBagCount())
                .status(WasteStatus.READY_FOR_COLLECTION)
                .build();

        WasteItem savedItem = wasteItemRepository.save(item);

        // Now build the tracking ID from that guaranteed-unique ID, and update
        String trackingId = trackingIdGenerator.generateFor(savedItem.getId());
        savedItem.setTrackingId(trackingId);
        savedItem.setQrCode(trackingId);
        savedItem = wasteItemRepository.save(savedItem);

        AppUser attender = principal.getUser();
        CollectionRequest collectionRequest = CollectionRequest.builder()
                .wasteItem(savedItem)
                .attender(attender)
                .status(RequestStatus.PENDING)
                .build();
        CollectionRequest savedRequest = collectionRequestRepository.save(collectionRequest);

        CollectionRequestResponse response = CollectionRequestResponse.builder()
                .id(savedRequest.getId())
                .wasteItemId(savedItem.getId())
                .trackingId(trackingId)
                .attenderId(attender.getId())
                .status(savedRequest.getStatus())
                .createdAt(savedRequest.getCreatedAt())
                .build();

        notificationRepository.save(com.smartmedwaste.backend.entity.Notification.builder()
                .user(attender).wasteItem(savedItem)
                .notificationType(com.smartmedwaste.backend.entity.enums.NotificationType.ACTIVITY)
                .message("You registered " + trackingId + " for collection.")
                .build());

        WasteItem finalSavedItem = savedItem;
        appUserRepository.findByOrganizationIdAndRole(dept.getOrganization().getId(),
                        com.smartmedwaste.backend.entity.enums.UserRole.COLLECTOR)
                .forEach(collector -> notificationRepository.save(
                        com.smartmedwaste.backend.entity.Notification.builder()
                                .user(collector).wasteItem(finalSavedItem)
                                .notificationType(com.smartmedwaste.backend.entity.enums.NotificationType.ACTIVITY)
                                .message("New waste ready for pickup: " + trackingId)
                                .build()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}