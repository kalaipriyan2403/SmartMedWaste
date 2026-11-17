package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.ClassificationSuggestionResponse;
import com.smartmedwaste.backend.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/classification")
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationService classificationService;

    @PostMapping(
            value = "/suggest",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("hasRole('DEPT_STAFF')")
    public ResponseEntity<?> suggest(
            @RequestParam("image") MultipartFile image
    ) {

        try {

            if (image == null || image.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message",
                                "Please upload an image."
                        ));
            }

            ClassificationSuggestionResponse suggestion =
                    classificationService.classify(image);

            return ResponseEntity.ok(suggestion);

        } catch (Exception ex) {

            ex.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "message",
                            "AI classification unavailable — please classify manually.",
                            "manualCategoriesAvailable",
                            true
                    ));
        }
    }
}