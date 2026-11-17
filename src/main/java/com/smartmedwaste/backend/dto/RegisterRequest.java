package com.smartmedwaste.backend.dto;

import com.smartmedwaste.backend.entity.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "Organization ID is required")
    private Long orgId;

    private Long deptId; // optional

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
    private String phone;

    @Email(message = "Invalid email format")
    private String email; // optional

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}