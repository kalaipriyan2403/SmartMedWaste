package com.smartmedwaste.backend.repository;
import java.util.List;
import java.util.Optional;

import com.smartmedwaste.backend.entity.CollectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRequestRepository extends JpaRepository<CollectionRequest, Long> {
    Optional<CollectionRequest> findByWasteItemId(Long wasteItemId);
    List<CollectionRequest> findByAttenderId(Long attenderId);
}