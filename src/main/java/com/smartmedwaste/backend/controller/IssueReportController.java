package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.IssueReportRequest;
import com.smartmedwaste.backend.entity.IssueReport;
import com.smartmedwaste.backend.repository.IssueReportRepository;
import com.smartmedwaste.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueReportController {

    private final IssueReportRepository issueReportRepository;

    public record IssueDto(
            Long id,
            String reporterName,
            String reporterRole,
            String trackingId,
            String message,
            String status,
            LocalDateTime createdAt
    ) {}

    // =========================================================
    // REPORT ISSUE
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('COLLECTOR') or hasRole('DRIVER') or hasRole('DISPOSAL_OPERATOR')")
    public ResponseEntity<?> report(
            @Valid @RequestBody IssueReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        IssueReport issue = IssueReport.builder()
                .reporter(principal.getUser())
                .trackingId(request.getTrackingId())
                .message(request.getMessage())
                .status("OPEN")
                .build();

        IssueReport saved = issueReportRepository.save(issue);

        // Reload with reporter to avoid LazyInitializationException
        IssueReport result = issueReportRepository
                .findByIdWithReporter(saved.getId())
                .orElseThrow();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDto(result));
    }

    // =========================================================
    // GET ALL ISSUES
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<IssueDto> getAll() {

        return issueReportRepository
                .findAllWithReporter()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // RESOLVE ISSUE
    // =========================================================

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolve(@PathVariable Long id) {

        // IMPORTANT:
        // Fetch issue + reporter together
        IssueReport issue = issueReportRepository
                .findByIdWithReporter(id)
                .orElse(null);

        if (issue == null) {
            return ResponseEntity.notFound().build();
        }

        // Change status
        issue.setStatus("RESOLVED");

        // Save updated issue
        IssueReport saved = issueReportRepository.save(issue);

        // Reload again with reporter
        // This guarantees reporter is available for toDto()
        IssueReport result = issueReportRepository
                .findByIdWithReporter(saved.getId())
                .orElseThrow();

        return ResponseEntity.ok(toDto(result));
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private IssueDto toDto(IssueReport issue) {

        return new IssueDto(
                issue.getId(),
                issue.getReporter().getName(),
                issue.getReporter().getRole().name(),
                issue.getTrackingId(),
                issue.getMessage(),
                issue.getStatus(),
                issue.getCreatedAt()
        );
    }
}