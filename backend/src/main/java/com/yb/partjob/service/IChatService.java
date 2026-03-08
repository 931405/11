package com.yb.partjob.service;

import com.yb.partjob.model.ChatMessage;
import java.util.List;

public interface IChatService {

    List<ChatMessage> getMessages(Long applicationId, Long userId);

    ChatMessage sendMessage(Long applicationId, Long userId, String content, String msgType);

    void markAsRead(Long applicationId, Long userId);

    long getUnreadCount(Long userId);

    List<com.yb.partjob.model.vo.ConversationVO> getConversations(Long userId);
}
