package com.yb.partjob.controller;

import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.DataIntegrationRecord;
import com.yb.partjob.model.dto.DataVerificationRequestDTO;
import com.yb.partjob.model.dto.JobPositionDTO;
import com.yb.partjob.model.vo.CandidateVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IEnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    @Autowired
    private IEnterpriseService enterpriseService;

    @GetMapping("/info")
    public Result<EnterpriseInfo> getInfo(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.getEnterpriseInfo(userId));
    }

    @PutMapping("/info")
    public Result<EnterpriseInfo> updateInfo(Authentication auth,
            @RequestBody EnterpriseInfo info) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.updateEnterpriseInfo(userId, info));
    }

    @PostMapping("/jobs")
    public Result<JobPosition> createJob(Authentication auth,
            @RequestBody JobPositionDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.createJob(userId, dto));
    }

    @PutMapping("/jobs/{jobId}")
    public Result<JobPosition> updateJob(Authentication auth,
            @PathVariable Long jobId,
            @RequestBody JobPositionDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.updateJob(userId, jobId, dto));
    }

    @DeleteMapping("/jobs/{jobId}")
    public Result<Void> deleteJob(Authentication auth, @PathVariable Long jobId) {
        Long userId = (Long) auth.getPrincipal();
        enterpriseService.deleteJob(userId, jobId);
        return Result.success();
    }

    @GetMapping("/jobs")
    public Result<Page<JobPosition>> getJobs(Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.getEnterpriseJobs(userId, status, page, size));
    }

    @GetMapping("/jobs/{jobId}/candidates")
    public Result<Page<CandidateVO>> getCandidates(Authentication auth,
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.getJobCandidates(userId, jobId, page, size));
    }

    @GetMapping("/student/{studentUserId}")
    public Result<StudentProfile> getStudentProfile(Authentication auth,
            @PathVariable Long studentUserId) {
        return Result.success(enterpriseService.getStudentProfile(studentUserId));
    }

    @PutMapping("/applications/{applicationId}")
    public Result<Void> updateApplicationStatus(Authentication auth,
            @PathVariable Long applicationId,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        Long userId = (Long) auth.getPrincipal();
        enterpriseService.updateApplicationStatus(userId, applicationId, status, remark);
        return Result.success();
    }

    @GetMapping("/talents")
    public Result<Page<CandidateVO>> searchTalents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String educationLevel,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String expectedLocation,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(enterpriseService.searchTalents(keyword, educationLevel, major, expectedLocation, page, size));
    }

    @GetMapping("/talents/filter-options")
    public Result<Map<String, List<String>>> getTalentFilterOptions() {
        return Result.success(enterpriseService.getTalentFilterOptions());
    }

    @PostMapping("/talents/invite")
    public Result<Long> inviteTalent(Authentication auth,
            @RequestBody com.yb.partjob.model.dto.InviteDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.inviteTalent(userId, dto));
    }

    @GetMapping("/dashboard/trend")
    public Result<com.yb.partjob.model.vo.TrendVO> getDailyApplicationTrend(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.getDailyApplicationTrend(userId));
    }

    @GetMapping("/dashboard/analytics")
    public Result<Map<String, Object>> getDashboardAnalytics(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.getEnterpriseAnalytics(userId));
    }

    @GetMapping("/jobs/{jobId}/matches")
    public Result<Page<CandidateVO>> getJobMatches(Authentication auth,
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.getJobMatches(userId, jobId, page, size));
    }

    @PostMapping("/jobs/{jobId}/verification-request")
    public Result<DataIntegrationRecord> submitVerificationRequest(Authentication auth,
            @PathVariable Long jobId,
            @RequestBody(required = false) DataVerificationRequestDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.submitJobVerificationRequest(userId, jobId,
                dto != null ? dto : new DataVerificationRequestDTO()));
    }
}
