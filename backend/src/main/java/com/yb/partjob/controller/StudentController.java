package com.yb.partjob.controller;

import com.yb.partjob.model.JobApplication;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.StudentProfileDTO;
import com.yb.partjob.model.vo.ApplicationVO;
import com.yb.partjob.model.vo.JobVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IJobService;
import com.yb.partjob.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IJobService jobService;

    @GetMapping("/profile")
    public Result<StudentProfile> getProfile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(studentService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<StudentProfile> updateProfile(Authentication auth,
            @RequestBody StudentProfileDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(studentService.updateProfile(userId, dto));
    }

    @GetMapping("/recommendations")
    public Result<Page<JobVO>> getRecommendations(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(studentService.getRecommendedJobs(userId, page, size));
    }

    @PostMapping("/apply/{jobId}")
    public Result<JobApplication> applyJob(Authentication auth,
            @PathVariable Long jobId,
            @RequestParam(required = false) String message) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(jobService.applyJob(userId, jobId, message));
    }

    @GetMapping("/applications")
    public Result<Page<ApplicationVO>> getApplications(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(jobService.getMyApplications(userId, page, size));
    }

    @PostMapping("/favorite/{jobId}")
    public Result<Void> favoriteJob(Authentication auth, @PathVariable Long jobId) {
        Long userId = (Long) auth.getPrincipal();
        jobService.favoriteJob(userId, jobId);
        return Result.success();
    }

    @DeleteMapping("/favorite/{jobId}")
    public Result<Void> unfavoriteJob(Authentication auth, @PathVariable Long jobId) {
        Long userId = (Long) auth.getPrincipal();
        jobService.unfavoriteJob(userId, jobId);
        return Result.success();
    }

    @GetMapping("/favorites")
    public Result<Page<JobVO>> getFavorites(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(jobService.getMyFavorites(userId, page, size));
    }

    @PostMapping("/resume/parse")
    public Result<com.yb.partjob.model.dto.ResumeParseResultDTO> parseResume(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return Result.success(studentService.parseResumePDF(file));
    }

    @GetMapping("/interviews")
    public Result<Page<com.yb.partjob.model.vo.InterviewInvitationVO>> getMyInvitations(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(studentService.getMyInvitations(userId, page, size));
    }

    @PutMapping("/interviews/{id}/status")
    public Result<Void> updateInvitationStatus(Authentication auth,
            @PathVariable Long id,
            @RequestParam String status) {
        Long userId = (Long) auth.getPrincipal();
        studentService.updateInvitationStatus(userId, id, status);
        return Result.success();
    }
}
