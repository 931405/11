package com.yb.partjob.controller;

import com.yb.partjob.model.ChatMessage;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {

    @Autowired
    private IChatService chatService;

    @GetMapping("/{applicationId}")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long applicationId,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(chatService.getMessages(applicationId, userId));
    }

    @PostMapping("/{applicationId}")
    public Result<ChatMessage> sendMessage(@PathVariable Long applicationId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String content = body.get("content");
        String msgType = body.getOrDefault("msgType", "TEXT");

        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }

        return Result.success(chatService.sendMessage(applicationId, userId, content.trim(), msgType));
    }

    @PutMapping("/{applicationId}/read")
    public Result<Void> markAsRead(@PathVariable Long applicationId,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        chatService.markAsRead(applicationId, userId);
        return Result.success(null);
    }

    @GetMapping("/unread")
    public Result<Long> getUnreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(chatService.getUnreadCount(userId));
    }

    @GetMapping("/conversations")
    public Result<List<com.yb.partjob.model.vo.ConversationVO>> getConversations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(chatService.getConversations(userId));
    }
}
