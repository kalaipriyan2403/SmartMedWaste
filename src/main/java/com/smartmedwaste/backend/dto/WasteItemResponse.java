package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder @AllArgsConstructor
public class WasteItemResponse {
    private Long id;
    private Long deptId;
    private WasteCategory category;
    private ColourCode colourCode;
    private BigDecimal quantity;
    private Unit unit;
    private Integer bagCount;
    private String trackingId;
    private WasteStatus status;
    private LocalDateTime createdAt;
}