package com.smartmedwaste.backend.repository;

import com.smartmedwaste.backend.entity.WasteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WasteItemRepository extends JpaRepository<WasteItem, Long> {

    Optional<WasteItem> findByTrackingId(String trackingId);

    @Query("""
        SELECT d.organization.id
        FROM WasteItem w
        JOIN w.department d
        WHERE w.id = :wasteItemId
    """)
    Optional<Long> findOrganizationIdByWasteItemId(
            @Param("wasteItemId") Long wasteItemId
    );
}