package com.yb.partjob.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StudentProfileDTO {
    private String university;
    private String major;
    private String educationLevel;
    private Integer enrollmentYear;
    private String skills;
    private String selfIntro;
    private BigDecimal expectedSalaryMin;
    private BigDecimal expectedSalaryMax;
    private String expectedLocation;
    private String availableSchedule;
    private String resumeAttachments;

    // Privacy settings
    private Boolean privacyResumeOpen;
    private Boolean privacyShowName;
    private Boolean privacyShowContact;
    private Boolean privacyShowOnline;
    private Boolean privacyShowLastActive;

    // Notification settings
    private Boolean notifyNewJob;
    private Boolean notifyApplicationStatus;
    private Boolean notifyInterview;
    private Boolean notifyNewMessage;
    private Boolean notifyMessageSound;
    private Boolean notifySystemAnnouncement;
    private Boolean notifyPromotion;
}
