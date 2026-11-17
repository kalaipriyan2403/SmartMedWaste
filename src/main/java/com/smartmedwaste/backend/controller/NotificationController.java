package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.repository.NotificationRepository;
import com.smartmedwaste.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public record NotificationDto(Long id, String message, String notificationType, boolean readStatus, LocalDateTime createdAt) {}

    @GetMapping
    public List<NotificationDto> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
            .limit(50)
            .map(n -> new NotificationDto(n.getId(), n.getMessage(), n.getNotificationType().name(), n.isReadStatus(), n.getCreatedAt()))
        .toList();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        var n = notificationRepository.findById(id).orElse(null);
        if (n == null || !n.getUser().getId().equals(principal.getId())) return ResponseEntity.notFound().build();
        n.setReadStatus(true);
        notificationRepository.save(n);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
            .filter(n -> !n.isReadStatus())
        .forEach(n -> { n.setReadStatus(true); notificationRepository.save(n); });
        return ResponseEntity.ok().build();
    }




}