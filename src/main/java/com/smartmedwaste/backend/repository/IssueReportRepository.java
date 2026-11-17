package com.smartmedwaste.backend.repository;

import com.smartmedwaste.backend.entity.IssueReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {

    // Get all issues along with reporter details
    @Query("""
        SELECT ir
        FROM IssueReport ir
        JOIN FETCH ir.reporter
        ORDER BY ir.createdAt DESC
    """)
    List<IssueReport> findAllWithReporter();

    // Get one issue along with reporter details
    // Used by the resolve API
    @Query("""
        SELECT ir
        FROM IssueReport ir
        JOIN FETCH ir.reporter
        WHERE ir.id = :id
    """)
    Optional<IssueReport> findByIdWithReporter(@Param("id") Long id);
}