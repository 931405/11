package com.yb.partjob.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CandidateVO {
    // Application fields
    private Long applicationId;
    private Long studentId;
    private Long studentUserId;
    private String status;
    private String applyMessage;
    private String enterpriseRemark;
    private LocalDateTime appliedAt;

    // Student user fields
    private String studentName;
    private String avatar;

    // Student profile fields
    private String university;
    private String major;
    private String educationLevel;
    private Integer enrollmentYear;
    private String skills; // JSON string
    private String selfIntro;
    private BigDecimal expectedSalaryMin;
    private BigDecimal expectedSalaryMax;
    private String expectedLocation;
    private String availableSchedule;
    private String resumeAttachments;

    // Match details
    private BigDecimal matchScore;
    private BigDecimal skillScore;
    private BigDecimal salaryScore;
    private BigDecimal locationScore;
    private BigDecimal scheduleScore;
    private Integer integratedSourceCount;
    private String recommendationReason;
}
