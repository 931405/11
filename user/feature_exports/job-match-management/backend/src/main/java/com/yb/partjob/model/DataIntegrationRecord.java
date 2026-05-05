package com.yb.partjob.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_integration_record", uniqueConstraints = {
        @UniqueConstraint(name = "uk_data_integration_source_target", columnNames = {"source_type", "target_type", "target_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataIntegrationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    @Column(name = "source_name", nullable = false, length = 100)
    private String sourceName;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    private Long submittedByUserId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String authorizationStatus = "NOT_REQUIRED";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String verificationStatus = "PENDING";

    @Column(precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    @Column(columnDefinition = "TEXT")
    private String normalizedPayload;

    @Column(length = 50)
    private String ruleVersion;

    @Column(length = 500)
    private String remark;

    private LocalDateTime lastVerifiedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
