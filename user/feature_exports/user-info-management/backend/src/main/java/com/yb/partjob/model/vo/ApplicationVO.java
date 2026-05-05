package com.yb.partjob.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApplicationVO {
    // Application core
    private Long id;
    private Long studentId;
    private Long jobId;
    private String status;
    private String applyMessage;
    private String enterpriseRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enriched job info
    private String jobTitle;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String workLocation;

    // Enriched company info
    private String companyName;
    private String companyIndustry;

    // Unread count
    private Long unreadCount;

    // Timeline events
    private List<TimelineEvent> timeline;

    @Data
    public static class TimelineEvent {
        private String label;
        private String description;
        private LocalDateTime time;
        private String type; // "primary", "success", "warning", "danger", "info"
    }
}
