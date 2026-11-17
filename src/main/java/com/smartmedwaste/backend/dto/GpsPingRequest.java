package com.smartmedwaste.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class GpsPingRequest {
    @NotNull
    private String trackingId;
    @NotNull
    private BigDecimal latitude;
    @NotNull
    private BigDecimal longitude;
}