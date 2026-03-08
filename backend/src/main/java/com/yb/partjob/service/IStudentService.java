package com.yb.partjob.service;

import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.StudentProfileDTO;
import com.yb.partjob.model.vo.JobVO;
import org.springframework.data.domain.Page;

public interface IStudentService {
    StudentProfile getProfile(Long userId);

    StudentProfile updateProfile(Long userId, StudentProfileDTO dto);

    Page<JobVO> getRecommendedJobs(Long userId, int page, int size);

    com.yb.partjob.model.dto.ResumeParseResultDTO parseResumePDF(org.springframework.web.multipart.MultipartFile file);

    Page<com.yb.partjob.model.vo.InterviewInvitationVO> getMyInvitations(Long studentUserId, int page, int size);

    void updateInvitationStatus(Long studentUserId, Long invitationId, String status);
}
