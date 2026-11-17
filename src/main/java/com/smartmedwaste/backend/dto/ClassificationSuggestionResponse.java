package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.ColourCode;
import com.smartmedwaste.backend.entity.enums.WasteCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class ClassificationSuggestionResponse {
    private WasteCategory category;
    private ColourCode colourCode;
    private double confidence;
    private double confidencePercentage;
    private boolean requiresManualReview;
}