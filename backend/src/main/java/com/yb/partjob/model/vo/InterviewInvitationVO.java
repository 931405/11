package com.yb.partjob.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewInvitationVO {
    private Long id;
    private Long applicationId;
    private Long enterpriseId;
    private Long studentId;
    private Long jobId;
    private LocalDateTime interviewTime;
    private String location;
    private String contact;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    // Additional info for display
    private String jobTitle;
    private String companyName;
    private String studentName;
}
