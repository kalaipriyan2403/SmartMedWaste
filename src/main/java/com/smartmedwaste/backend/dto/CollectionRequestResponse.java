package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder @AllArgsConstructor
public class CollectionRequestResponse {
    private Long id;
    private Long wasteItemId;
    private String trackingId;
    private Long attenderId;
    private RequestStatus status;
    private LocalDateTime createdAt;
}