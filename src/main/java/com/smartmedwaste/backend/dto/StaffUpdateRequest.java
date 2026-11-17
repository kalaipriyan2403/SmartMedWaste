package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StaffUpdateRequest {
    private String name;
    private UserRole role;
    private Long deptId; // nullable — can be reassigned or cleared
}