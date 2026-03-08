package com.yb.partjob.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(length = 100)
    private String university;

    @Column(length = 100)
    private String major;

    @Column(length = 20)
    private String educationLevel;

    private Integer enrollmentYear;

    @Column(columnDefinition = "JSON")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String selfIntro;

    @Column(precision = 10, scale = 2)
    private BigDecimal expectedSalaryMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal expectedSalaryMax;

    @Column(length = 200)
    private String expectedLocation;

    @Column(length = 500)
    private String availableSchedule;

    @Column(columnDefinition = "JSON")
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
