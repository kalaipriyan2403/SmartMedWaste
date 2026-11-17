package com.smartmedwaste.backend.repository;

import com.smartmedwaste.backend.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {}