package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdminCreateStaffRequest {

    @NotBlank
    private String name;

    @NotNull(message = "Role is required")
    private UserRole role; // ADMIN, ATTENDER, COLLECTOR, DRIVER, DISPOSAL_OPERATOR

    @NotNull
    private Long orgId;

    private Long deptId; // optional

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
    private String phone;

    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;
}