package com.yb.partjob.controller;

import com.yb.partjob.model.JobApplication;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.ResumeParseResultDTO;
import com.yb.partjob.model.dto.StudentProfileDTO;
import com.yb.partjob.model.vo.ApplicationVO;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.model.vo.JobVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IJobService;
import com.yb.partjob.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IJobService jobService;

    @GetMapping("/profile")
    public Result<StudentProfile> getProfile(Authentication auth) {
        return Result.success(studentService.getProfile((Long) auth.getPrincipal()));
    }

    @PutMapping("/profile")
    public Result<StudentProfile> updateProfile(Authentication auth, @RequestBody StudentProfileDTO dto) {
        return Result.success(studentService.updateProfile((Long) auth.getPrincipal(), dto));
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(Authentication auth) {
        return Result.success(studentService.getDashboardSummary((Long) auth.getPrincipal()));
    }

    @GetMapping("/recommendations")
    public Result<Page<JobVO>> getRecommendations(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(studentService.getRecommendedJobs((Long) auth.getPrincipal(), page, size));
    }

    @PostMapping("/apply/{jobId}")
    public Result<JobApplication> applyJob(Authentication auth, @PathVariable Long jobId,
            @RequestParam(required = false) String message) {
        return Result.success(jobService.applyJob((Long) auth.getPrincipal(), jobId, message));
    }

    @GetMapping("/applications")
    public Result<Page<ApplicationVO>> getApplications(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(jobService.getMyApplications((Long) auth.getPrincipal(), page, size));
    }

    @PostMapping("/favorite/{jobId}")
    public Result<Void> favoriteJob(Authentication auth, @PathVariable Long jobId) {
        jobService.favoriteJob((Long) auth.getPrincipal(), jobId);
        return Result.success();
    }

    @DeleteMapping("/favorite/{jobId}")
    public Result<Void> unfavoriteJob(Authentication auth, @PathVariable Long jobId) {
        jobService.unfavoriteJob((Long) auth.getPrincipal(), jobId);
        return Result.success();
    }

    @GetMapping("/favorites")
    public Result<Page<JobVO>> getFavorites(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(jobService.getMyFavorites((Long) auth.getPrincipal(), page, size));
    }

    @PostMapping("/resume/parse")
    public Result<ResumeParseResultDTO> parseResume(@RequestParam("file") MultipartFile file) {
        return Result.success(studentService.parseResumePDF(file));
    }

    @GetMapping("/interviews")
    public Result<Page<InterviewInvitationVO>> getInterviews(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(studentService.getMyInvitations((Long) auth.getPrincipal(), page, size));
    }

    @PutMapping("/interviews/{id}/status")
    public Result<Void> updateInterviewStatus(Authentication auth, @PathVariable Long id, @RequestParam String status) {
        studentService.updateInvitationStatus((Long) auth.getPrincipal(), id, status);
        return Result.success();
    }
}
