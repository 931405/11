package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.*;
import com.yb.partjob.repository.*;
import com.yb.partjob.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements IAdminService {

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private JobCategoryRepository categoryRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Autowired
    private SysLogRepository sysLogRepository;

    @Override
    public Page<EnterpriseInfo> getPendingAudits(int page, int size) {
        return enterpriseInfoRepository.findByCertificationStatus("PENDING",
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt")));
    }

    @Override
    @Transactional
    public void auditEnterprise(Long enterpriseId, String status, String remark) {
        EnterpriseInfo info = enterpriseInfoRepository.findById(enterpriseId)
                .orElseThrow(() -> new BusinessException("Enterprise not found"));

        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("Invalid certification status");
        }

        info.setCertificationStatus(status);
        info.setCertificationRemark(remark);
        enterpriseInfoRepository.save(info);
    }

    @Override
    public Page<SysUser> getUserList(String role, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (role != null && !role.isEmpty()) {
            return userRepository.findByRole(role, pageRequest);
        }
        return userRepository.findAll(pageRequest);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public Page<Feedback> getFeedbackList(String statusFilter, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (statusFilter != null && !statusFilter.isEmpty()) {
            return feedbackRepository.findByStatus(statusFilter, pageRequest);
        }
        return feedbackRepository.findAll(pageRequest);
    }

    @Override
    @Transactional
    public void replyFeedback(Long feedbackId, String reply) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException("Feedback not found"));
        feedback.setAdminReply(reply);
        feedback.setStatus("RESOLVED");
        feedbackRepository.save(feedback);
    }

    @Override
    @Transactional
    public JobCategory createCategory(String name, Long parentId, Integer sortOrder) {
        JobCategory category = JobCategory.builder()
                .name(name)
                .parentId(parentId != null ? parentId : 0L)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .status(1)
                .build();
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void updateCategory(Long categoryId, String name, Integer sortOrder, Integer status) {
        JobCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("Category not found"));
        if (name != null)
            category.setName(name);
        if (sortOrder != null)
            category.setSortOrder(sortOrder);
        if (status != null)
            category.setStatus(status);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException("Category not found");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public Page<JobPosition> getJobList(String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isEmpty()) {
            return jobPositionRepository.findByStatus(status, pageRequest); // note: need findByStatus in repository
        }
        return jobPositionRepository.findAll(pageRequest);
    }

    @Override
    @Transactional
    public void updateJobStatus(Long jobId, String status) {
        JobPosition job = jobPositionRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found"));
        job.setStatus(status);
        jobPositionRepository.save(job);
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", userRepository.countByRole("STUDENT"));
        stats.put("totalEnterprises", userRepository.countByRole("ENTERPRISE"));
        stats.put("totalAdmins", userRepository.countByRole("ADMIN"));
        stats.put("totalJobs", jobPositionRepository.countByStatus("OPEN"));
        stats.put("pendingAudits", enterpriseInfoRepository.countByCertificationStatus("PENDING"));
        stats.put("pendingFeedback", feedbackRepository.countByStatus("PENDING"));
        stats.put("totalApplications", applicationRepository.count());

        List<String> trendDays = new ArrayList<>();
        List<Long> trendStudentData = new ArrayList<>();
        List<Long> trendAppData = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            trendDays.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            trendStudentData.add(userRepository.countByRoleAndCreatedAtBetween("STUDENT", startOfDay, endOfDay));
            trendAppData.add(applicationRepository.countByCreatedAtBetween(startOfDay, endOfDay));
        }

        stats.put("trendDays", trendDays);
        stats.put("trendStudentData", trendStudentData);
        stats.put("trendAppData", trendAppData);

        return stats;
    }

    @Override
    public Map<String, String> getMatchSettings() {
        Map<String, String> settings = new HashMap<>();
        sysConfigRepository.findAll().forEach(config -> settings.put(config.getConfigKey(), config.getConfigValue()));
        return settings;
    }

    @Override
    @Transactional
    public void saveMatchSettings(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            SysConfig config = sysConfigRepository.findByConfigKey(key)
                    .orElse(SysConfig.builder().configKey(key).description("Algorithm weight").build());
            config.setConfigValue(String.valueOf(value));
            sysConfigRepository.save(config);
        });
    }

    @Override
    public Page<SysLog> getSystemLogs(String action, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (action != null && !action.isEmpty()) {
            return sysLogRepository.findByActionContaining(action, pageRequest);
        }
        return sysLogRepository.findAll(pageRequest);
    }
}
