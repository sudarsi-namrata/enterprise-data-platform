package com.enterprise.platform.repository;

import com.enterprise.platform.model.DataRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface DataRecordRepository extends JpaRepository<DataRecord, Long> {

    boolean existsByContentHash(String contentHash);

    Page<DataRecord> findBySourceIdOrderByIngestedAtDesc(Long sourceId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM DataRecord r WHERE r.source.id = :sourceId AND r.status = :status")
    long countBySourceAndStatus(@Param("sourceId") Long sourceId, @Param("status") DataRecord.RecordStatus status);

    @Query("SELECT COUNT(r) FROM DataRecord r WHERE r.source.id = :sourceId " +
           "AND r.ingestedAt BETWEEN :start AND :end")
    long countBySourceAndDateRange(@Param("sourceId") Long sourceId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);
}
