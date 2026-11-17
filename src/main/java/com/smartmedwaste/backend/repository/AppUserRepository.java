package com.smartmedwaste.backend.repository;

import com.smartmedwaste.backend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByPhone(String phone);
    Optional<AppUser> findByEmail(String email);
    List<AppUser> findByOrganizationIdAndRole(Long orgId, com.smartmedwaste.backend.entity.enums.UserRole role);
}