package com.smartmedwaste.backend.repository;

import com.smartmedwaste.backend.entity.DisposalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisposalRecordRepository extends JpaRepository<DisposalRecord, Long> {}