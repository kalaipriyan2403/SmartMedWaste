package com.smartmedwaste.backend.util;

import com.smartmedwaste.backend.entity.enums.ColourCode;
import com.smartmedwaste.backend.entity.enums.WasteCategory;

public final class GovtColourMapper {

    private GovtColourMapper() {}

    public static ColourCode colourFor(WasteCategory category) {
        return switch (category) {
            case ANATOMICAL -> ColourCode.YELLOW;
            case CONTAMINATED_RECYCLABLE -> ColourCode.RED;
            case SHARPS -> ColourCode.WHITE;
            case PHARMA_GLASS -> ColourCode.BLUE;
        };
    }
}