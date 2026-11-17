package com.smartmedwaste.backend.util;

import org.springframework.stereotype.Component;

import java.time.Year;

@Component
public class TrackingIdGenerator {

    /**
     * Builds a tracking ID from the WasteItem's own database ID — which is
     * guaranteed unique and persists correctly across app restarts, unlike
     * an in-memory counter that resets to 1 every restart.
     */
    public String generateFor(Long wasteItemId) {
        return String.format("BW-%d-%05d", Year.now().getValue(), wasteItemId);
    }
}