package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.Unit;
import com.smartmedwaste.backend.entity.enums.WasteCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class EodRegistrationRequest {

    @NotNull(message = "Category is required")
    private WasteCategory category;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Unit is required")
    private Unit unit;

    @NotNull(message = "Bag count is required")
    @Min(value = 1, message = "Bag count must be at least 1")
    private Integer bagCount;

    @NotNull(message = "Department ID is required")
    private Long deptId;
}