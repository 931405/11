package com.yb.partjob.model.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ConversationVO {
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private Long studentId;
    private String studentName;
    private String studentAvatar;
    private Long enterpriseId;
    private String companyName;
    private String companyAvatar;

    private String latestMessage;
    private String latestMsgType;
    private LocalDateTime latestMessageTime;
    private Long unreadCount;
}
