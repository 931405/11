package com.yb.partjob.service;

import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.DataIntegrationRecord;
import com.yb.partjob.model.dto.DataAuthorizationDTO;
import com.yb.partjob.model.dto.StudentProfileDTO;
import com.yb.partjob.model.vo.JobVO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IStudentService {
    StudentProfile getProfile(Long userId);

    StudentProfile updateProfile(Long userId, StudentProfileDTO dto);

    Page<JobVO> getRecommendedJobs(Long userId, int page, int size);

    com.yb.partjob.model.dto.ResumeParseResultDTO parseResumePDF(org.springframework.web.multipart.MultipartFile file);

    Page<com.yb.partjob.model.vo.InterviewInvitationVO> getMyInvitations(Long studentUserId, int page, int size);

    void updateInvitationStatus(Long studentUserId, Long invitationId, String status);

    Map<String, Object> getDashboardSummary(Long userId);

    void saveDataAuthorization(Long userId, DataAuthorizationDTO dto);

    List<DataIntegrationRecord> getMyIntegrationRecords(Long userId);
}
