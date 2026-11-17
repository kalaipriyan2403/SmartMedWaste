package com.smartmedwaste.backend.service;
import com.smartmedwaste.backend.dto.AdminCreateStaffRequest;
import com.smartmedwaste.backend.dto.StaffResponse;

import com.smartmedwaste.backend.dto.AuthResponse;
import com.smartmedwaste.backend.dto.LoginRequest;
import com.smartmedwaste.backend.dto.RegisterRequest;
import com.smartmedwaste.backend.entity.AppUser;
import com.smartmedwaste.backend.entity.Department;
import com.smartmedwaste.backend.entity.Organization;
import com.smartmedwaste.backend.entity.enums.UserStatus;
import com.smartmedwaste.backend.repository.AppUserRepository;
import com.smartmedwaste.backend.repository.DepartmentRepository;
import com.smartmedwaste.backend.repository.OrganizationRepository;
import com.smartmedwaste.backend.security.JwtService;
import com.smartmedwaste.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() != com.smartmedwaste.backend.entity.enums.UserRole.DEPT_STAFF) {
            throw new IllegalArgumentException(
                    "Public self-registration is only available for Department Staff. " +
                            "Other roles must be created by an Organization Admin."
            );
        }
        if (appUserRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        if (request.getEmail() != null && appUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        Organization org = organizationRepository.findById(request.getOrgId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid organization ID"));

        Department dept = null;
        if (request.getDeptId() != null) {
            dept = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid department ID"));
        }

        AppUser user = AppUser.builder()
                .name(request.getName())
                .role(request.getRole())
                .organization(org)
                .department(dept)
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .build();

        AppUser saved = appUserRepository.save(user);

        String token = jwtService.generateToken(new UserPrincipal(saved));

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .name(saved.getName())
                .role(saved.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
        );

        AppUser user = appUserRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(new UserPrincipal(user));

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
    public StaffResponse adminCreateStaff(AdminCreateStaffRequest request) {
        if (request.getRole() == com.smartmedwaste.backend.entity.enums.UserRole.DEPT_STAFF) {
            throw new IllegalArgumentException(
                    "Dept Staff must self-register via /api/auth/register, not through Admin creation."
            );
        }

        if (appUserRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        if (request.getEmail() != null && appUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        Organization org = organizationRepository.findById(request.getOrgId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid organization ID"));

        Department dept = null;
        if (request.getDeptId() != null) {
            dept = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid department ID"));
        }

        AppUser user = AppUser.builder()
                .name(request.getName())
                .role(request.getRole())
                .organization(org)
                .department(dept)
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .build();

        AppUser saved = appUserRepository.save(user);

        return StaffResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .role(saved.getRole())
                .orgId(saved.getOrganization().getId())
                .deptId(saved.getDepartment() != null ? saved.getDepartment().getId() : null)
                .phone(saved.getPhone())
                .email(saved.getEmail())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}