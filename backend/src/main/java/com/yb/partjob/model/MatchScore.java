package com.yb.partjob.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal skillScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal salaryScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal locationScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal scheduleScore;

    private LocalDateTime computedAt;

    @PrePersist
    protected void onCreate() {
        computedAt = LocalDateTime.now();
    }
}
