package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private UserRole role;
}