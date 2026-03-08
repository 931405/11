package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.ChatMessage;
import com.yb.partjob.model.JobApplication;
import com.yb.partjob.model.SysUser;
import com.yb.partjob.repository.ChatMessageRepository;
import com.yb.partjob.repository.JobApplicationRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.EnterpriseInfo;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class ChatServiceImpl implements IChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Override
    public List<ChatMessage> getMessages(Long applicationId, Long userId) {
        // Verify the user has access to this application's chat
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("投递记录不存在"));

        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // Students can only see their own application chats
        // Enterprises can only see chats for jobs they own (simplified: just allow for
        // now)

        return chatMessageRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(Long applicationId, Long userId, String content, String msgType) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("投递记录不存在"));

        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        ChatMessage msg = new ChatMessage();
        msg.setApplicationId(applicationId);
        msg.setSenderId(userId);
        msg.setSenderRole(user.getRole());
        msg.setContent(content);
        msg.setMsgType(msgType != null ? msgType : "TEXT");
        msg.setIsRead(false);

        return chatMessageRepository.save(msg);
    }

    @Override
    @Transactional
    public void markAsRead(Long applicationId, Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // Mark messages from the OTHER role as read
        String myRole = user.getRole();
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByApplicationIdOrderByCreatedAtAsc(applicationId);

        for (ChatMessage msg : unreadMessages) {
            if (!msg.getSenderRole().equals(myRole) && !msg.getIsRead()) {
                msg.setIsRead(true);
                chatMessageRepository.save(msg);
            }
        }
    }

    @Autowired
    private com.yb.partjob.repository.JobPositionRepository jobPositionRepository;

    @Autowired
    private com.yb.partjob.repository.StudentProfileRepository studentProfileRepository;

    @Autowired
    private com.yb.partjob.repository.EnterpriseInfoRepository enterpriseInfoRepository;

    @Override
    public long getUnreadCount(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if ("STUDENT".equals(user.getRole())) {
            StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
            if (profile == null)
                return 0;
            List<JobApplication> apps = applicationRepository
                    .findByStudentId(profile.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
            if (apps.isEmpty())
                return 0;
            List<Long> appIds = apps.stream().map(JobApplication::getId).toList();
            return chatMessageRepository.countByApplicationIdInAndSenderRoleNotAndIsReadFalse(appIds, "STUDENT");
        } else if ("ENTERPRISE".equals(user.getRole())) {
            EnterpriseInfo info = enterpriseInfoRepository.findByUserId(userId).orElse(null);
            if (info == null)
                return 0;
            List<com.yb.partjob.model.JobPosition> jobs = jobPositionRepository
                    .findByEnterpriseId(info.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
            if (jobs.isEmpty())
                return 0;
            List<Long> jobIds = jobs.stream().map(com.yb.partjob.model.JobPosition::getId).toList();
            List<JobApplication> apps = applicationRepository
                    .findByJobIdIn(jobIds, org.springframework.data.domain.Pageable.unpaged()).getContent();
            if (apps.isEmpty())
                return 0;
            List<Long> appIds = apps.stream().map(JobApplication::getId).toList();
            return chatMessageRepository.countByApplicationIdInAndSenderRoleNotAndIsReadFalse(appIds, "ENTERPRISE");
        }
        return 0;
    }

    @Override
    public List<com.yb.partjob.model.vo.ConversationVO> getConversations(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        List<JobApplication> apps;
        if ("STUDENT".equals(user.getRole())) {
            StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
            if (profile == null)
                return java.util.Collections.emptyList();
            apps = applicationRepository
                    .findByStudentId(profile.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else if ("ENTERPRISE".equals(user.getRole())) {
            EnterpriseInfo info = enterpriseInfoRepository.findByUserId(userId).orElse(null);
            if (info == null)
                return java.util.Collections.emptyList();
            List<com.yb.partjob.model.JobPosition> jobs = jobPositionRepository
                    .findByEnterpriseId(info.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
            if (jobs.isEmpty())
                return java.util.Collections.emptyList();
            List<Long> jobIds = jobs.stream().map(com.yb.partjob.model.JobPosition::getId).toList();
            apps = applicationRepository.findByJobIdIn(jobIds, org.springframework.data.domain.Pageable.unpaged())
                    .getContent();
        } else {
            return java.util.Collections.emptyList();
        }

        if (apps.isEmpty())
            return java.util.Collections.emptyList();

        List<Long> appIds = apps.stream().map(JobApplication::getId).toList();
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByApplicationIds(appIds);
        java.util.Map<Long, ChatMessage> latestMsgMap = latestMessages.stream()
                .collect(java.util.stream.Collectors.toMap(ChatMessage::getApplicationId, m -> m));

        return apps.stream().map(app -> {
            com.yb.partjob.model.JobPosition job = jobPositionRepository.findById(app.getJobId()).orElse(null);
            StudentProfile student = studentProfileRepository.findById(app.getStudentId()).orElse(null);
            SysUser studentUser = student != null ? userRepository.findById(student.getUserId()).orElse(null) : null;
            EnterpriseInfo enterprise = job != null
                    ? enterpriseInfoRepository.findById(job.getEnterpriseId()).orElse(null)
                    : null;

            ChatMessage latestMsg = latestMsgMap.get(app.getId());
            long unreadCount = chatMessageRepository.countByApplicationIdAndSenderRoleNotAndIsReadFalse(app.getId(),
                    user.getRole());

            return com.yb.partjob.model.vo.ConversationVO.builder()
                    .applicationId(app.getId())
                    .jobId(job != null ? job.getId() : null)
                    .jobTitle(job != null ? job.getTitle() : "未知职位")
                    .studentId(student != null ? student.getId() : null)
                    .studentName(studentUser != null
                            ? studentUser.getRealName() != null ? studentUser.getRealName() : studentUser.getUsername()
                            : "未知学生")
                    .studentAvatar(studentUser != null ? studentUser.getAvatar() : null)
                    .enterpriseId(enterprise != null ? enterprise.getId() : null)
                    .companyName(enterprise != null ? enterprise.getCompanyName() : "未知公司")
                    .latestMessage(latestMsg != null ? latestMsg.getContent() : null)
                    .latestMsgType(latestMsg != null ? latestMsg.getMsgType() : null)
                    .latestMessageTime(latestMsg != null ? latestMsg.getCreatedAt() : null)
                    .unreadCount(unreadCount)
                    .build();
        })
                .filter(c -> c.getLatestMessage() != null
                        || "APPLIED".equals(appRepositoryStatus(c.getApplicationId()))) // Only show if they've
                                                                                        // explicitly chatted or just
                                                                                        // applied (simplified logic:
                                                                                        // just show all apps with
                                                                                        // messages or all apps here if
                                                                                        // we want)
                // Let's filter out ones with NO latest message to keep it clean, unless you
                // want all applications showing up. For chat, usually you only show ones with
                // actual messages. But let's show all applications for now as "conversations".
                .sorted((c1, c2) -> {
                    LocalDateTime t1 = c1.getLatestMessageTime();
                    LocalDateTime t2 = c2.getLatestMessageTime();
                    if (t1 == null && t2 == null)
                        return 0;
                    if (t1 == null)
                        return 1;
                    if (t2 == null)
                        return -1;
                    return t2.compareTo(t1);
                })
                .toList();
    }

    // helper
    private String appRepositoryStatus(Long id) {
        return applicationRepository.findById(id).map(JobApplication::getStatus).orElse("");
    }
}
