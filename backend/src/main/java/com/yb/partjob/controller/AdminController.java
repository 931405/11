package com.yb.partjob.controller;

import com.yb.partjob.model.*;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(adminService.getDashboardStats());
    }

    @GetMapping("/audits")
    public Result<Page<EnterpriseInfo>> getPendingAudits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getPendingAudits(page, size));
    }

    @PutMapping("/audits/{enterpriseId}")
    public Result<Void> auditEnterprise(@PathVariable Long enterpriseId,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        adminService.auditEnterprise(enterpriseId, status, remark);
        return Result.success();
    }

    @GetMapping("/users")
    public Result<Page<SysUser>> getUserList(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getUserList(role, page, size));
    }

    @PutMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId,
            @RequestParam Integer status) {
        adminService.updateUserStatus(userId, status);
        return Result.success();
    }

    @GetMapping("/feedback")
    public Result<Page<Feedback>> getFeedbackList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getFeedbackList(status, page, size));
    }

    @PutMapping("/feedback/{feedbackId}/reply")
    public Result<Void> replyFeedback(@PathVariable Long feedbackId,
            @RequestParam String reply) {
        adminService.replyFeedback(feedbackId, reply);
        return Result.success();
    }

    @PostMapping("/categories")
    public Result<JobCategory> createCategory(@RequestParam String name,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer sortOrder) {
        return Result.success(adminService.createCategory(name, parentId, sortOrder));
    }

    @PutMapping("/categories/{categoryId}")
    public Result<Void> updateCategory(@PathVariable Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) Integer status) {
        adminService.updateCategory(categoryId, name, sortOrder, status);
        return Result.success();
    }

    @DeleteMapping("/categories/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        adminService.deleteCategory(categoryId);
        return Result.success();
    }

    @GetMapping("/jobs")
    public Result<Page<JobPosition>> getJobList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getJobList(status, page, size));
    }

    @PutMapping("/jobs/{jobId}/status")
    public Result<Void> updateJobStatus(@PathVariable Long jobId,
            @RequestParam String status) {
        adminService.updateJobStatus(jobId, status);
        return Result.success();
    }

    @GetMapping("/settings/match")
    public Result<Map<String, String>> getMatchSettings() {
        return Result.success(adminService.getMatchSettings());
    }

    @PutMapping("/settings/match")
    public Result<Void> saveMatchSettings(@RequestBody Map<String, String> settings) {
        adminService.saveMatchSettings(settings);
        return Result.success();
    }

    @GetMapping("/logs")
    public Result<Page<SysLog>> getSystemLogs(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getSystemLogs(action, page, size));
    }
}
