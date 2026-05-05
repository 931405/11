package com.yb.partjob.controller;

import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.InviteDTO;
import com.yb.partjob.model.dto.JobPositionDTO;
import com.yb.partjob.model.vo.CandidateVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.model.vo.TrendVO;
import com.yb.partjob.service.IEnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    @Autowired
    private IEnterpriseService enterpriseService;

    @GetMapping("/info")
    public Result<EnterpriseInfo> getInfo(Authentication auth) {
        return Result.success(enterpriseService.getEnterpriseInfo((Long) auth.getPrincipal()));
    }

    @PutMapping("/info")
    public Result<EnterpriseInfo> updateInfo(Authentication auth, @RequestBody EnterpriseInfo info) {
        return Result.success(enterpriseService.updateEnterpriseInfo((Long) auth.getPrincipal(), info));
    }

    @PostMapping("/jobs")
    public Result<JobPosition> createJob(Authentication auth, @RequestBody JobPositionDTO dto) {
        return Result.success(enterpriseService.createJob((Long) auth.getPrincipal(), dto));
    }

    @PutMapping("/jobs/{jobId}")
    public Result<JobPosition> updateJob(Authentication auth, @PathVariable Long jobId, @RequestBody JobPositionDTO dto) {
        return Result.success(enterpriseService.updateJob((Long) auth.getPrincipal(), jobId, dto));
    }

    @DeleteMapping("/jobs/{jobId}")
    public Result<Void> deleteJob(Authentication auth, @PathVariable Long jobId) {
        enterpriseService.deleteJob((Long) auth.getPrincipal(), jobId);
        return Result.success();
    }

    @GetMapping("/jobs")
    public Result<Page<JobPosition>> getJobs(Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(enterpriseService.getEnterpriseJobs((Long) auth.getPrincipal(), status, page, size));
    }

    @GetMapping("/jobs/{jobId}/candidates")
    public Result<Page<CandidateVO>> getCandidates(Authentication auth,
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(enterpriseService.getJobCandidates((Long) auth.getPrincipal(), jobId, page, size));
    }

    @GetMapping("/jobs/{jobId}/matches")
    public Result<Page<CandidateVO>> getJobMatches(Authentication auth,
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(enterpriseService.getJobMatches((Long) auth.getPrincipal(), jobId, page, size));
    }

    @PutMapping("/applications/{applicationId}/status")
    public Result<Void> updateApplicationStatus(Authentication auth,
            @PathVariable Long applicationId,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        enterpriseService.updateApplicationStatus((Long) auth.getPrincipal(), applicationId, status, remark);
        return Result.success();
    }

    @GetMapping("/student/{studentUserId}")
    public Result<StudentProfile> getStudentProfile(@PathVariable Long studentUserId) {
        return Result.success(enterpriseService.getStudentProfile(studentUserId));
    }

    @GetMapping("/talents")
    public Result<Page<CandidateVO>> searchTalents(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(enterpriseService.searchTalents(keyword, page, size));
    }

    @PostMapping("/talents/invite")
    public Result<Long> inviteTalent(Authentication auth, @RequestBody InviteDTO dto) {
        return Result.success(enterpriseService.inviteTalent((Long) auth.getPrincipal(), dto));
    }

    @GetMapping("/dashboard/trend")
    public Result<TrendVO> getDailyApplicationTrend(Authentication auth) {
        return Result.success(enterpriseService.getDailyApplicationTrend((Long) auth.getPrincipal()));
    }

    @GetMapping("/dashboard/analytics")
    public Result<Map<String, Object>> getAnalytics(Authentication auth) {
        return Result.success(enterpriseService.getEnterpriseAnalytics((Long) auth.getPrincipal()));
    }
}
