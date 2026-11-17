package com.smartmedwaste.backend.controller;

import com.smartmedwaste.backend.dto.AuthResponse;
import com.smartmedwaste.backend.dto.LoginRequest;
import com.smartmedwaste.backend.dto.RegisterRequest;
import com.smartmedwaste.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid phone or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@org.springframework.security.core.annotation.AuthenticationPrincipal
                                com.smartmedwaste.backend.security.UserPrincipal principal) {
        var u = principal.getUser();
        return ResponseEntity.ok(java.util.Map.of(
                "id", u.getId(),
                "name", u.getName(),
                "phone", u.getPhone(),
                "email", u.getEmail() == null ? "" : u.getEmail(),
                "role", u.getRole().name(),
                "orgId", u.getOrganization().getId(),
                "deptId", u.getDepartment() != null ? u.getDepartment().getId() : 0,
                "status", u.getStatus().name(),
                "createdAt", u.getCreatedAt().toString()
        ));
    }

}