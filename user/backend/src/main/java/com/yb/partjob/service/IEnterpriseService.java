package com.yb.partjob.service;

import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.InterviewInvitationDTO;
import com.yb.partjob.model.dto.InviteDTO;
import com.yb.partjob.model.dto.JobPositionDTO;
import com.yb.partjob.model.vo.CandidateVO;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.model.vo.TrendVO;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface IEnterpriseService {
    EnterpriseInfo getEnterpriseInfo(Long userId);

    EnterpriseInfo updateEnterpriseInfo(Long userId, EnterpriseInfo info);

    JobPosition createJob(Long userId, JobPositionDTO dto);

    JobPosition updateJob(Long userId, Long jobId, JobPositionDTO dto);

    void deleteJob(Long userId, Long jobId);

    Page<JobPosition> getEnterpriseJobs(Long userId, String status, int page, int size);

    Page<CandidateVO> getJobCandidates(Long userId, Long jobId, int page, int size);

    StudentProfile getStudentProfile(Long studentUserId);

    Page<CandidateVO> searchTalents(String keyword, int page, int size);

    Long inviteTalent(Long enterpriseUserId, InviteDTO dto);

    TrendVO getDailyApplicationTrend(Long userId);

    void updateApplicationStatus(Long userId, Long applicationId, String status, String remark);

    void sendInterviewInvitation(Long enterpriseUserId, InterviewInvitationDTO dto);

    Page<InterviewInvitationVO> getEnterpriseInvitations(Long enterpriseUserId, int page, int size);

    Page<CandidateVO> getJobMatches(Long userId, Long jobId, int page, int size);

    Map<String, Object> getEnterpriseAnalytics(Long userId);
}
