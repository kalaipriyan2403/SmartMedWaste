package com.smartmedwaste.backend.service;

import com.smartmedwaste.backend.dto.ClassificationSuggestionResponse;
import com.smartmedwaste.backend.entity.enums.ColourCode;
import com.smartmedwaste.backend.entity.enums.WasteCategory;
import com.smartmedwaste.backend.util.GovtColourMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final RestTemplate restTemplate;

    @Value("${ai.classification.service-url}")
    private String aiServiceUrl;

    public ClassificationSuggestionResponse classify(
            MultipartFile image) {

        try {

            String url = aiServiceUrl.replaceAll("/+$", "")
                    + "/predict";

            System.out.println("======================================");
            System.out.println("AI REQUEST START");
            System.out.println("URL: " + url);
            System.out.println("Filename: " + image.getOriginalFilename());
            System.out.println("Size: " + image.getSize());
            System.out.println("Content Type: " + image.getContentType());
            System.out.println("======================================");

            ByteArrayResource resource =
                    new ByteArrayResource(image.getBytes()) {

                        @Override
                        public String getFilename() {
                            return image.getOriginalFilename() != null
                                    ? image.getOriginalFilename()
                                    : "image.jpg";
                        }
                    };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body =
                    new LinkedMultiValueMap<>();

            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            long start = System.currentTimeMillis();

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            Map.class
                    );

            long time =
                    System.currentTimeMillis() - start;

            System.out.println("======================================");
            System.out.println("AI RESPONSE RECEIVED");
            System.out.println("HTTP STATUS: " +
                    response.getStatusCode());
            System.out.println("TIME: " + time + " ms");
            System.out.println("BODY: " + response.getBody());
            System.out.println("======================================");

            Map<String, Object> aiResponse =
                    response.getBody();

            if (aiResponse == null) {
                throw new RuntimeException(
                        "AI service returned empty response");
            }

            String categoryString =
                    String.valueOf(
                            aiResponse.get("category"));

            WasteCategory category =
                    WasteCategory.valueOf(categoryString);

            ColourCode colourCode =
                    GovtColourMapper.colourFor(category);

            double confidence =
                    ((Number) aiResponse.get("confidence"))
                            .doubleValue();

            double confidencePercentage =
                    ((Number) aiResponse
                            .get("confidence_percentage"))
                            .doubleValue();

            boolean requiresManualReview =
                    Boolean.parseBoolean(
                            String.valueOf(
                                    aiResponse.get(
                                            "requires_manual_review")));

            return ClassificationSuggestionResponse.builder()
                    .category(category)
                    .colourCode(colourCode)
                    .confidence(confidence)
                    .confidencePercentage(
                            confidencePercentage)
                    .requiresManualReview(
                            requiresManualReview)
                    .build();

        } catch (Exception e) {

            System.err.println(
                    "======================================");
            System.err.println(
                    "AI REQUEST FAILED");
            System.err.println(
                    "Exception: " +
                            e.getClass().getName());
            System.err.println(
                    "Message: " +
                            e.getMessage());
            System.err.println(
                    "======================================");

            throw new RuntimeException(
                    "AI classification service unavailable",
                    e);
        }
    }
}