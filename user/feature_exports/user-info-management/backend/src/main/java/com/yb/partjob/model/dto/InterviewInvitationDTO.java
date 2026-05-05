package com.yb.partjob.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewInvitationDTO {
    private Long applicationId;
    private LocalDateTime interviewTime;
    private String location;
    private String contact;
    private String message;
}
