package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.JobApplication;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.JobPositionDTO;
import com.yb.partjob.model.vo.CandidateVO;
import com.yb.partjob.model.vo.TrendVO;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.JobApplicationRepository;
import com.yb.partjob.repository.JobPositionRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.repository.ChatMessageRepository;
import com.yb.partjob.model.ChatMessage;
import com.yb.partjob.model.dto.InviteDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.yb.partjob.repository.InterviewInvitationRepository;
import com.yb.partjob.model.InterviewInvitation;
import com.yb.partjob.model.dto.InterviewInvitationDTO;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.service.IEnterpriseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnterpriseServiceImpl implements IEnterpriseService {

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private InterviewInvitationRepository interviewInvitationRepository;

    @Override
    public EnterpriseInfo getEnterpriseInfo(Long userId) {
        return enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));
    }

    @Override
    public EnterpriseInfo updateEnterpriseInfo(Long userId, EnterpriseInfo info) {
        EnterpriseInfo existing = enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        if (info.getCompanyName() != null)
            existing.setCompanyName(info.getCompanyName());
        if (info.getIndustry() != null)
            existing.setIndustry(info.getIndustry());
        if (info.getCompanySize() != null)
            existing.setCompanySize(info.getCompanySize());
        if (info.getContactPerson() != null)
            existing.setContactPerson(info.getContactPerson());
        if (info.getContactPhone() != null)
            existing.setContactPhone(info.getContactPhone());
        if (info.getCompanyAddress() != null)
            existing.setCompanyAddress(info.getCompanyAddress());
        if (info.getBusinessLicense() != null)
            existing.setBusinessLicense(info.getBusinessLicense());
        if (info.getDescription() != null)
            existing.setDescription(info.getDescription());

        return enterpriseInfoRepository.save(existing);
    }

    @Override
    @Transactional
    public JobPosition createJob(Long userId, JobPositionDTO dto) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        if (!"APPROVED".equals(enterprise.getCertificationStatus())) {
            throw new BusinessException("Enterprise must be certified before posting jobs");
        }

        JobPosition job = JobPosition.builder()
                .enterpriseId(enterprise.getId())
                .title(dto.getTitle())
                .categoryId(dto.getCategoryId())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .skillsRequired(dto.getSkillsRequired())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .workLocation(dto.getWorkLocation())
                .workSchedule(dto.getWorkSchedule())
                .headcount(dto.getHeadcount() != null ? dto.getHeadcount() : 1)
                .status(dto.getStatus() != null ? dto.getStatus() : "OPEN")
                .viewCount(0)
                .applyCount(0)
                .build();

        return jobPositionRepository.save(job);
    }

    @Override
    @Transactional
    public JobPosition updateJob(Long userId, Long jobId, JobPositionDTO dto) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        JobPosition job = jobPositionRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found"));

        if (!job.getEnterpriseId().equals(enterprise.getId())) {
            throw new BusinessException("Not authorized to update this job");
        }

        if (dto.getTitle() != null)
            job.setTitle(dto.getTitle());
        if (dto.getCategoryId() != null)
            job.setCategoryId(dto.getCategoryId());
        if (dto.getDescription() != null)
            job.setDescription(dto.getDescription());
        if (dto.getRequirements() != null)
            job.setRequirements(dto.getRequirements());
        if (dto.getSkillsRequired() != null)
            job.setSkillsRequired(dto.getSkillsRequired());
        if (dto.getSalaryMin() != null)
            job.setSalaryMin(dto.getSalaryMin());
        if (dto.getSalaryMax() != null)
            job.setSalaryMax(dto.getSalaryMax());
        if (dto.getWorkLocation() != null)
            job.setWorkLocation(dto.getWorkLocation());
        if (dto.getWorkSchedule() != null)
            job.setWorkSchedule(dto.getWorkSchedule());
        if (dto.getHeadcount() != null)
            job.setHeadcount(dto.getHeadcount());
        if (dto.getStatus() != null)
            job.setStatus(dto.getStatus());

        return jobPositionRepository.save(job);
    }

    @Override
    @Transactional
    public void deleteJob(Long userId, Long jobId) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        JobPosition job = jobPositionRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found"));

        if (!job.getEnterpriseId().equals(enterprise.getId())) {
            throw new BusinessException("Not authorized to delete this job");
        }

        jobPositionRepository.delete(job);
    }

    @Override
    public Page<JobPosition> getEnterpriseJobs(Long userId, String status, int page, int size) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        if (status != null && !status.trim().isEmpty()) {
            return jobPositionRepository.findByEnterpriseIdAndStatus(enterprise.getId(), status,
                    PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        } else {
            return jobPositionRepository.findByEnterpriseId(enterprise.getId(),
                    PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        }
    }

    @Override
    public Page<CandidateVO> getJobCandidates(Long userId, Long jobId, int page, int size) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        JobPosition job = jobPositionRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found"));

        if (!job.getEnterpriseId().equals(enterprise.getId())) {
            throw new BusinessException("Not authorized to view candidates for this job");
        }

        Page<JobApplication> applications = applicationRepository.findByJobId(jobId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return applications.map(app -> {
            CandidateVO vo = new CandidateVO();
            vo.setApplicationId(app.getId());
            vo.setStudentId(app.getStudentId());
            vo.setStatus(app.getStatus());
            vo.setApplyMessage(app.getApplyMessage());
            vo.setEnterpriseRemark(app.getEnterpriseRemark());
            vo.setAppliedAt(app.getCreatedAt());

            // Enrich with student user info
            sysUserRepository.findById(app.getStudentId()).ifPresent(user -> {
                vo.setStudentName(user.getRealName());
                vo.setAvatar(user.getAvatar());
            });

            // Enrich with student profile info
            studentProfileRepository.findByUserId(app.getStudentId()).ifPresent(profile -> {
                vo.setUniversity(profile.getUniversity());
                vo.setMajor(profile.getMajor());
                vo.setEducationLevel(profile.getEducationLevel());
                vo.setEnrollmentYear(profile.getEnrollmentYear());
                vo.setSkills(profile.getSkills());
                vo.setSelfIntro(profile.getSelfIntro());
                vo.setExpectedSalaryMin(profile.getExpectedSalaryMin());
                vo.setExpectedSalaryMax(profile.getExpectedSalaryMax());
                vo.setExpectedLocation(profile.getExpectedLocation());
                vo.setAvailableSchedule(profile.getAvailableSchedule());
            });

            return vo;
        });
    }

    @Override
    public StudentProfile getStudentProfile(Long studentUserId) {
        return studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new BusinessException("Student profile not found"));
    }

    @Override
    public Page<CandidateVO> searchTalents(String keyword, int page, int size) {
        Page<StudentProfile> profiles = studentProfileRepository.searchPublicProfiles(
                keyword,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updatedAt")));

        return profiles.map(profile -> {
            CandidateVO vo = new CandidateVO();
            vo.setStudentId(profile.getUserId());
            vo.setUniversity(profile.getUniversity());
            vo.setMajor(profile.getMajor());
            vo.setEducationLevel(profile.getEducationLevel());
            vo.setEnrollmentYear(profile.getEnrollmentYear());
            vo.setSkills(profile.getSkills());
            vo.setSelfIntro(profile.getSelfIntro());
            vo.setExpectedSalaryMin(profile.getExpectedSalaryMin());
            vo.setExpectedSalaryMax(profile.getExpectedSalaryMax());
            vo.setExpectedLocation(profile.getExpectedLocation());
            vo.setAvailableSchedule(profile.getAvailableSchedule());

            sysUserRepository.findById(profile.getUserId()).ifPresent(user -> {
                vo.setStudentName(user.getRealName());
                vo.setAvatar(user.getAvatar());
            });

            return vo;
        });
    }

    @Override
    @Transactional
    public Long inviteTalent(Long enterpriseUserId, InviteDTO dto) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(enterpriseUserId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        JobPosition job = jobPositionRepository.findById(dto.getJobId())
                .orElseThrow(() -> new BusinessException("Job not found"));

        if (!job.getEnterpriseId().equals(enterprise.getId())) {
            throw new BusinessException("Not authorized to invite for this job");
        }

        if (applicationRepository.existsByStudentIdAndJobId(dto.getStudentId(), dto.getJobId())) {
            throw new BusinessException("已存在该职位的投递或邀请记录");
        }

        JobApplication application = JobApplication.builder()
                .studentId(dto.getStudentId())
                .jobId(dto.getJobId())
                .status("INVITED")
                .build();

        JobApplication savedApp = applicationRepository.save(application);

        ChatMessage message = ChatMessage.builder()
                .applicationId(savedApp.getId())
                .senderId(enterpriseUserId)
                .senderRole("ENTERPRISE")
                .content(dto.getGreeting())
                .msgType("TEXT")
                .isRead(false)
                .build();

        chatMessageRepository.save(message);

        return savedApp.getId();
    }

    @Override
    @Transactional
    public void updateApplicationStatus(Long userId, Long applicationId, String status, String remark) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));

        JobPosition job = jobPositionRepository.findById(application.getJobId())
                .orElseThrow(() -> new BusinessException("Job not found"));

        if (!job.getEnterpriseId().equals(enterprise.getId())) {
            throw new BusinessException("Not authorized to update this application");
        }

        application.setStatus(status);
        if (remark != null) {
            application.setEnterpriseRemark(remark);
        }
        applicationRepository.save(application);
    }

    @Override
    public TrendVO getDailyApplicationTrend(Long enterpriseUserId) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(enterpriseUserId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        List<JobPosition> jobs = jobPositionRepository
                .findByEnterpriseId(enterprise.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        if (jobs.isEmpty()) {
            return generateEmptyTrend();
        }

        List<Long> jobIds = jobs.stream().map(JobPosition::getId).collect(Collectors.toList());
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0)
                .withNano(0);

        List<JobApplication> apps = applicationRepository.findByJobIdInAndCreatedAtAfter(jobIds, sevenDaysAgo);

        Map<String, Long> countMap = apps.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getCreatedAt().getMonthValue() + "/" + app.getCreatedAt().getDayOfMonth(),
                        Collectors.counting()));

        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDateTime d = LocalDateTime.now().minusDays(i);
            String dateStr = d.getMonthValue() + "/" + d.getDayOfMonth();
            dates.add(dateStr);
            counts.add(countMap.getOrDefault(dateStr, 0L).intValue());
        }

        return new TrendVO(dates, counts);
    }

    private TrendVO generateEmptyTrend() {
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime d = LocalDateTime.now().minusDays(i);
            dates.add(d.getMonthValue() + "/" + d.getDayOfMonth());
            counts.add(0);
        }
        return new TrendVO(dates, counts);
    }

    @Override
    @Transactional
    public void sendInterviewInvitation(Long enterpriseUserId, InterviewInvitationDTO dto) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(enterpriseUserId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        JobApplication application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new BusinessException("Application not found"));

        JobPosition job = jobPositionRepository.findById(application.getJobId())
                .orElseThrow(() -> new BusinessException("Job not found"));

        if (!job.getEnterpriseId().equals(enterprise.getId())) {
            throw new BusinessException("Not authorized to send invitation for this job");
        }

        // Prevent duplicate invites for the same application
        if (interviewInvitationRepository.existsByApplicationId(application.getId())) {
            throw new BusinessException("Interview invitation already sent for this application");
        }

        InterviewInvitation invitation = InterviewInvitation.builder()
                .applicationId(application.getId())
                .enterpriseId(enterprise.getId())
                .studentId(application.getStudentId())
                .jobId(job.getId())
                .interviewTime(dto.getInterviewTime())
                .location(dto.getLocation())
                .contact(dto.getContact())
                .message(dto.getMessage())
                .status("PENDING")
                .build();

        interviewInvitationRepository.save(invitation);

        // Automatically update the application status
        application.setStatus("INTERVIEW");
        applicationRepository.save(application);

        // Send a chat message notification
        studentProfileRepository.findByUserId(application.getStudentId())
                .orElseThrow(() -> new BusinessException("Student profile not found"));

        String chatMessageContent = String.format(
                "【面试邀请】您好，我们诚挚邀请您参加面试！\n面试时间: %s\n面试地点/链接: %s\n联系人: %s%s",
                dto.getInterviewTime().toString(),
                dto.getLocation(),
                dto.getContact(),
                (dto.getMessage() != null && !dto.getMessage().isEmpty()) ? "\n附加信息: " + dto.getMessage() : "");

        ChatMessage message = ChatMessage.builder()
                .applicationId(application.getId())
                .senderId(enterpriseUserId)
                .senderRole("ENTERPRISE")
                .content(chatMessageContent)
                .msgType("TEXT")
                .isRead(false)
                .build();

        chatMessageRepository.save(message);
    }

    @Override
    public Page<InterviewInvitationVO> getEnterpriseInvitations(Long enterpriseUserId, int page, int size) {
        EnterpriseInfo enterprise = enterpriseInfoRepository.findByUserId(enterpriseUserId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        Page<InterviewInvitation> invitations = interviewInvitationRepository.findByEnterpriseId(
                enterprise.getId(),
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return invitations.map(inv -> {
            InterviewInvitationVO vo = new InterviewInvitationVO();
            BeanUtils.copyProperties(inv, vo);

            jobPositionRepository.findById(inv.getJobId())
                    .ifPresent(job -> vo.setJobTitle(job.getTitle()));

            vo.setCompanyName(enterprise.getCompanyName());

            sysUserRepository.findById(inv.getStudentId())
                    .ifPresent(user -> vo.setStudentName(user.getRealName()));

            return vo;
        });
    }
}
