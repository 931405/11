package com.yb.partjob.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false, length = 20)
    private String senderRole;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(length = 20)
    @Builder.Default
    private String msgType = "TEXT";

    @Column
    @Builder.Default
    private Boolean isRead = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
