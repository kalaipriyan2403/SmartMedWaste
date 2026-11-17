package com.smartmedwaste.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VehicleRequest {
    @NotBlank
    private String vehicleNo;
}