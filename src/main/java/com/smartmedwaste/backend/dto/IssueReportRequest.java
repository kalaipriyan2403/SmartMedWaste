package com.smartmedwaste.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class IssueReportRequest {
    private String trackingId; // optional
    @NotBlank
    private String message;
}