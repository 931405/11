package com.yb.partjob.repository;

import com.yb.partjob.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

    long countByApplicationIdAndSenderRoleNotAndIsReadFalse(Long applicationId, String senderRole);

    long countByApplicationIdInAndSenderRoleNotAndIsReadFalse(List<Long> applicationIds, String senderRole);

    @Query("SELECT m1 FROM ChatMessage m1 LEFT JOIN ChatMessage m2 " +
            "ON m1.applicationId = m2.applicationId AND m1.createdAt < m2.createdAt " +
            "WHERE m2.id IS NULL AND m1.applicationId IN :applicationIds")
    List<ChatMessage> findLatestMessagesByApplicationIds(
            @org.springframework.data.repository.query.Param("applicationIds") List<Long> applicationIds);
}
