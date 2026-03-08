package com.yb.partjob.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobVO {
    private Long id;
    private String title;
    private Long enterpriseId;
    private String jobType;
    private String companyName;
    private String categoryName;
    private String description;
    private String requirements;
    private String skillsRequired;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String workLocation;
    private String workSchedule;
    private String educationRequirement;
    private String durationRequirement;
    private String jobTags;
    private Integer headcount;

    // Additional Company Info for right sidebar
    private String companyScale;
    private String companyIndustry;
    private String status;
    private Integer viewCount;
    private Integer applyCount;
    private LocalDateTime createdAt;
    private BigDecimal matchScore;
    private Boolean isFavorited;
    private Boolean isApplied;
}
