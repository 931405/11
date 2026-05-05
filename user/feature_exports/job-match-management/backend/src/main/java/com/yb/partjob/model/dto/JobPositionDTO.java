package com.yb.partjob.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class JobPositionDTO {
    private String title;
    private String jobType;
    private Long categoryId;
    private String description;
    private String requirements;
    private String skillsRequired;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String workLocation;
    private String workSchedule;
    private Integer headcount;
    private String status;
}
