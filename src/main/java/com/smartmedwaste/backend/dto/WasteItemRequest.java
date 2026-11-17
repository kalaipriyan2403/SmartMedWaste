package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.Unit;
import com.smartmedwaste.backend.entity.enums.WasteCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class WasteItemRequest {

    @NotNull(message = "Category is required")
    private WasteCategory category;

    private java.math.BigDecimal aiConfidence; // optional, from AI suggestion

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Unit is required")
    private Unit unit;
}