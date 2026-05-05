package com.yb.partjob.service;

import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.ResumeParseResultDTO;
import com.yb.partjob.model.dto.StudentProfileDTO;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.model.vo.JobVO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IStudentService {
    StudentProfile getProfile(Long userId);

    StudentProfile updateProfile(Long userId, StudentProfileDTO dto);

    Page<JobVO> getRecommendedJobs(Long userId, int page, int size);

    ResumeParseResultDTO parseResumePDF(MultipartFile file);

    Page<InterviewInvitationVO> getMyInvitations(Long studentUserId, int page, int size);

    void updateInvitationStatus(Long studentUserId, Long invitationId, String status);

    Map<String, Object> getDashboardSummary(Long userId);
}
