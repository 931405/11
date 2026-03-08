package com.yb.partjob.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_invitation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long enterpriseId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private LocalDateTime interviewTime;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false, length = 100)
    private String contact;

    @Column(length = 500)
    private String message;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED, CANCELLED

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
