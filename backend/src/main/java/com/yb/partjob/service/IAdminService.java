package com.yb.partjob.service;

import com.yb.partjob.model.*;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface IAdminService {
    Page<EnterpriseInfo> getPendingAudits(int page, int size);

    void auditEnterprise(Long enterpriseId, String status, String remark);

    Page<SysUser> getUserList(String role, int page, int size);

    void updateUserStatus(Long userId, Integer status);

    Page<Feedback> getFeedbackList(String statusFilter, int page, int size);

    void replyFeedback(Long feedbackId, String reply);

    JobCategory createCategory(String name, Long parentId, Integer sortOrder);

    void updateCategory(Long categoryId, String name, Integer sortOrder, Integer status);

    void deleteCategory(Long categoryId);

    Page<JobPosition> getJobList(String status, int page, int size);

    void updateJobStatus(Long jobId, String status);

    Map<String, Object> getDashboardStats();

    Map<String, String> getMatchSettings();

    void saveMatchSettings(Map<String, String> settings);

    Page<SysLog> getSystemLogs(String action, int page, int size);
}
