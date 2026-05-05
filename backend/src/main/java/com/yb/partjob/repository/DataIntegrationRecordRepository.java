package com.yb.partjob.repository;

import com.yb.partjob.model.DataIntegrationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DataIntegrationRecordRepository extends JpaRepository<DataIntegrationRecord, Long> {

    Optional<DataIntegrationRecord> findFirstBySourceTypeAndTargetTypeAndTargetId(String sourceType,
            String targetType,
            Long targetId);

    List<DataIntegrationRecord> findByTargetTypeAndTargetIdOrderByUpdatedAtDesc(String targetType, Long targetId);

    long countByVerificationStatus(String verificationStatus);

    long countByTargetType(String targetType);

    long countByVerificationStatusAndTargetType(String verificationStatus, String targetType);

    @Query("select coalesce(avg(r.qualityScore), 0) from DataIntegrationRecord r")
    BigDecimal averageQualityScore();

    @Query("""
            select r from DataIntegrationRecord r
            where (:targetType is null or :targetType = '' or r.targetType = :targetType)
              and (:verificationStatus is null or :verificationStatus = '' or r.verificationStatus = :verificationStatus)
              and (:sourceType is null or :sourceType = '' or r.sourceType = :sourceType)
            order by r.updatedAt desc
            """)
    Page<DataIntegrationRecord> search(@Param("targetType") String targetType,
            @Param("verificationStatus") String verificationStatus,
            @Param("sourceType") String sourceType,
            Pageable pageable);
}
