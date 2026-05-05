package com.yb.partjob.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_position")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long enterpriseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String jobType;

    private Long categoryId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "JSON")
    private String skillsRequired;

    @Column(precision = 10, scale = 2)
    private BigDecimal salaryMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 200)
    private String workLocation;

    @Column(length = 500)
    private String workSchedule;

    @Column(length = 50)
    private String educationRequirement;

    @Column(length = 100)
    private String durationRequirement; // e.g., "4天/周 3个月"

    @Column(columnDefinition = "JSON")
    private String jobTags; // e.g., ["就近租房补贴", "餐饮及下午茶"]

    @Builder.Default
    private Integer headcount = 1;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";

    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer applyCount = 0;

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
