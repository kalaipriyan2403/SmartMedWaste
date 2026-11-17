package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.UserRole;
import com.smartmedwaste.backend.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder @AllArgsConstructor
public class StaffResponse {
    private Long id;
    private String name;
    private UserRole role;
    private Long orgId;
    private Long deptId;
    private String phone;
    private String email;
    private UserStatus status;
    private LocalDateTime createdAt;
}