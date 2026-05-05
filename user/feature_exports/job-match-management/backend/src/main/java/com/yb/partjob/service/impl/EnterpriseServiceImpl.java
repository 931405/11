package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.ChatMessage;
import com.yb.partjob.model.DataIntegrationRecord;
import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.InterviewInvitation;
import com.yb.partjob.model.JobApplication;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.MatchScore;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.DataVerificationRequestDTO;
import com.yb.partjob.model.dto.InterviewInvitationDTO;
import com.yb.partjob.model.dto.InviteDTO;
import com.yb.partjob.model.dto.JobPositionDTO;
import com.yb.partjob.model.vo.CandidateVO;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.model.vo.TrendVO;
import com.yb.partjob.repository.ChatMessageRepository;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.InterviewInvitationRepository;
import com.yb.partjob.repository.JobApplicationRepository;
import com.yb.partjob.repository.JobPositionRepository;
import com.yb.partjob.repository.MatchScoreRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IEnterpriseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private MatchScoreRepository matchScoreRepository;

    @Autowired
    private DataIntegrationService dataIntegrationService;

    @Override
    public EnterpriseInfo getEnterpriseInfo(Long userId) {
        return enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));
    }

    @Override
    public EnterpriseInfo updateEnterpriseInfo(Long userId, EnterpriseInfo info) {
        EnterpriseInfo existing = enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));

        if (info.getCompanyName() != null) existing.setCompanyName(info.getCompanyName());
        if (info.getIndustry() != null) existing.setIndustry(info.getIndustry());
        if (info.getCompanySize() != null) existing.setCompanySize(info.getCompanySize());
        if (info.getContactPerson() != null) existing.setContactPerson(info.getContactPerson());
        if (info.getContactPhone() != null) existing.setContactPhone(info.getContactPhone());
        if (info.getCompanyAddress() != null) existing.setCompanyAddress(info.getCompanyAddress());
        if (info.getBusinessLicense() != null) existing.setBusinessLicense(info.getBusinessLicense());
        if (info.getDescription() != null) existing.setDescription(info.getDescription());

        EnterpriseInfo saved = enterpriseInfoRepository.save(existing);
        dataIntegrationService.syncEnterpriseProfile(userId, saved);
        return saved;
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
                .jobType(dto.getJobType())
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

        JobPosition saved = jobPositionRepository.save(job);
        dataIntegrationService.syncJobProfile(userId, saved);
        return saved;
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

        if (dto.getTitle() != null) job.setTitle(dto.getTitle());
        if (dto.getJobType() != null) job.setJobType(dto.getJobType());
        if (dto.getCategoryId() != null) job.setCategoryId(dto.getCategoryId());
        if (dto.getDescription() != null) job.setDescription(dto.getDescription());
        if (dto.getRequirements() != null) job.setRequirements(dto.getRequirements());
        if (dto.getSkillsRequired() != null) job.setSkillsRequired(dto.getSkillsRequired());
        if (dto.getSalaryMin() != null) job.setSalaryMin(dto.getSalaryMin());
        if (dto.getSalaryMax() != null) job.setSalaryMax(dto.getSalaryMax());
        if (dto.getWorkLocation() != null) job.setWorkLocation(dto.getWorkLocation());
        if (dto.getWorkSchedule() != null) job.setWorkSchedule(dto.getWorkSchedule());
        if (dto.getHeadcount() != null) job.setHeadcount(dto.getHeadcount());
        if (dto.getStatus() != null) job.setStatus(dto.getStatus());

        JobPosition saved = jobPositionRepository.save(job);
        dataIntegrationService.syncJobProfile(userId, saved);
        return saved;
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
        }
        return jobPositionRepository.findByEnterpriseId(enterprise.getId(),
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Override
    public Page<CandidateVO> getJobCandidates(Long userId, Long jobId, int page, int size) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        JobPosition job = requireOwnedJob(enterprise, jobId);

        Page<JobApplication> applications = applicationRepository.findByJobId(jobId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return applications.map(app -> buildCandidateVO(resolveStudentProfileByProfileId(app.getStudentId()), app, job.getId(),
                true));
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

        return profiles.map(profile -> buildCandidateVO(profile, null, null, false));
    }

    @Override
    @Transactional
    public Long inviteTalent(Long enterpriseUserId, InviteDTO dto) {
        EnterpriseInfo enterprise = requireEnterprise(enterpriseUserId);
        JobPosition job = requireOwnedJob(enterprise, dto.getJobId());

        StudentProfile studentProfile = studentProfileRepository.findByUserId(dto.getStudentId())
                .orElseGet(() -> studentProfileRepository.findById(dto.getStudentId()).orElse(null));
        if (studentProfile == null) {
            throw new BusinessException("Student profile not found");
        }

        if (applicationRepository.existsByStudentIdAndJobId(studentProfile.getId(), dto.getJobId())) {
            throw new BusinessException("Candidate already has an application or invitation for this job");
        }

        JobApplication application = JobApplication.builder()
                .studentId(studentProfile.getId())
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
        EnterpriseInfo enterprise = requireEnterprise(userId);

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found"));

        JobPosition job = requireOwnedJob(enterprise, application.getJobId());
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
        EnterpriseInfo enterprise = requireEnterprise(enterpriseUserId);

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
        EnterpriseInfo enterprise = requireEnterprise(enterpriseUserId);

        JobApplication application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new BusinessException("Application not found"));

        JobPosition job = requireOwnedJob(enterprise, application.getJobId());

        if (interviewInvitationRepository.existsByApplicationId(application.getId())) {
            throw new BusinessException("Interview invitation already sent for this application");
        }

        studentProfileRepository.findById(application.getStudentId())
                .orElseThrow(() -> new BusinessException("Student profile not found"));

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

        application.setStatus("INTERVIEW");
        applicationRepository.save(application);

        String chatMessageContent = String.format(
                "Interview invitation%nTime: %s%nLocation: %s%nContact: %s%s",
                dto.getInterviewTime(),
                dto.getLocation(),
                dto.getContact(),
                (dto.getMessage() != null && !dto.getMessage().isBlank()) ? "%nNote: " + dto.getMessage() : "");

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
        EnterpriseInfo enterprise = requireEnterprise(enterpriseUserId);

        Page<InterviewInvitation> invitations = interviewInvitationRepository.findByEnterpriseId(
                enterprise.getId(),
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return invitations.map(inv -> {
            InterviewInvitationVO vo = new InterviewInvitationVO();
            BeanUtils.copyProperties(inv, vo);

            jobPositionRepository.findById(inv.getJobId())
                    .ifPresent(job -> vo.setJobTitle(job.getTitle()));

            vo.setCompanyName(enterprise.getCompanyName());

            studentProfileRepository.findById(inv.getStudentId())
                    .flatMap(profile -> sysUserRepository.findById(profile.getUserId()))
                    .ifPresent(user -> vo.setStudentName(user.getRealName()));

            return vo;
        });
    }

    @Override
    public Page<CandidateVO> getJobMatches(Long userId, Long jobId, int page, int size) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        requireOwnedJob(enterprise, jobId);

        Page<MatchScore> matches = matchScoreRepository.findByJobIdOrderByTotalScoreDesc(jobId,
                PageRequest.of(page - 1, size));

        return matches.map(match -> buildCandidateVO(resolveStudentProfileByProfileId(match.getStudentId()), null, jobId,
                true));
    }

    @Override
    public Map<String, Object> getEnterpriseAnalytics(Long userId) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        List<JobPosition> jobs = jobPositionRepository.findByEnterpriseId(enterprise.getId(),
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<Long> jobIds = jobs.stream().map(JobPosition::getId).toList();

        long openJobs = jobs.stream().filter(job -> "OPEN".equals(job.getStatus())).count();
        long closedJobs = jobs.size() - openJobs;
        long totalApplications = jobs.stream().mapToLong(job -> applicationRepository.countByJobId(job.getId())).sum();
        long pendingApplications = jobs.stream()
                .mapToLong(job -> applicationRepository.countByJobIdAndStatus(job.getId(), "APPLIED")
                        + applicationRepository.countByJobIdAndStatus(job.getId(), "REVIEWING"))
                .sum();
        long interviewingApplications = jobs.stream()
                .mapToLong(job -> applicationRepository.countByJobIdAndStatus(job.getId(), "INTERVIEW"))
                .sum();
        long matchedTalents = jobIds.stream()
                .mapToLong(jobId -> matchScoreRepository.countByJobIdAndTotalScoreGreaterThanEqual(jobId,
                        new BigDecimal("75")))
                .sum();

        BigDecimal averageMatchScore = BigDecimal.ZERO;
        List<MatchScore> sampleScores = new ArrayList<>();
        for (Long jobId : jobIds) {
            sampleScores.addAll(matchScoreRepository.findByJobIdOrderByTotalScoreDesc(jobId, PageRequest.of(0, 20))
                    .getContent());
        }
        if (!sampleScores.isEmpty()) {
            BigDecimal total = sampleScores.stream()
                    .map(MatchScore::getTotalScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageMatchScore = total.divide(BigDecimal.valueOf(sampleScores.size()), 2, RoundingMode.HALF_UP);
        }

        List<DataIntegrationRecord> enterpriseRecords = dataIntegrationService.getTargetRecords("ENTERPRISE", enterprise.getId());
        long verifiedRecords = enterpriseRecords.stream()
                .filter(record -> "VERIFIED".equals(record.getVerificationStatus()))
                .count();
        long pendingVerificationRequests = jobIds.stream()
                .flatMap(id -> dataIntegrationService.getTargetRecords("JOB", id).stream())
                .filter(record -> "JOB_VERIFICATION_REQUEST".equals(record.getSourceType())
                        && "PENDING".equals(record.getVerificationStatus()))
                .count();

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("activeJobs", openJobs);
        analytics.put("closedJobs", closedJobs);
        analytics.put("totalApplications", totalApplications);
        analytics.put("pendingApplications", pendingApplications);
        analytics.put("interviewingApplications", interviewingApplications);
        analytics.put("matchedTalents", matchedTalents);
        analytics.put("averageMatchScore", averageMatchScore);
        analytics.put("verifiedRecords", verifiedRecords);
        analytics.put("pendingVerificationRequests", pendingVerificationRequests);
        analytics.put("trend", getDailyApplicationTrend(userId));
        return analytics;
    }

    @Override
    public DataIntegrationRecord submitJobVerificationRequest(Long userId, Long jobId, DataVerificationRequestDTO dto) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        JobPosition job = requireOwnedJob(enterprise, jobId);
        DataVerificationRequestDTO payload = dto != null ? dto : new DataVerificationRequestDTO();
        return dataIntegrationService.submitJobVerificationRequest(userId, job, payload, enterprise.getCompanyName());
    }

    private EnterpriseInfo requireEnterprise(Long userId) {
        return enterpriseInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Enterprise info not found"));
    }

    private JobPosition requireOwnedJob(EnterpriseInfo enterprise, Long jobId) {
        JobPosition job = jobPositionRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found"));
        if (!job.getEnterpriseId().equals(enterprise.getId())) {
            throw new BusinessException("Not authorized to access this job");
        }
        return job;
    }

    private StudentProfile resolveStudentProfileByProfileId(Long profileId) {
        return studentProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Student profile not found"));
    }

    private CandidateVO buildCandidateVO(StudentProfile profile, JobApplication application, Long jobId, boolean includeMatch) {
        CandidateVO vo = new CandidateVO();
        vo.setStudentId(profile.getId());
        vo.setStudentUserId(profile.getUserId());
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
        vo.setResumeAttachments(profile.getResumeAttachments());

        sysUserRepository.findById(profile.getUserId()).ifPresent(user -> {
            vo.setStudentName(user.getRealName());
            vo.setAvatar(user.getAvatar());
        });

        if (application != null) {
            vo.setApplicationId(application.getId());
            vo.setStatus(application.getStatus());
            vo.setApplyMessage(application.getApplyMessage());
            vo.setEnterpriseRemark(application.getEnterpriseRemark());
            vo.setAppliedAt(application.getCreatedAt());
        }

        if (includeMatch && jobId != null) {
            matchScoreRepository.findByStudentIdAndJobId(profile.getId(), jobId).ifPresent(match -> {
                vo.setMatchScore(match.getTotalScore());
                vo.setSkillScore(match.getSkillScore());
                vo.setSalaryScore(match.getSalaryScore());
                vo.setLocationScore(match.getLocationScore());
                vo.setScheduleScore(match.getScheduleScore());
                vo.setRecommendationReason(buildRecommendationReason(match));
            });
        }

        vo.setIntegratedSourceCount(dataIntegrationService.getTargetRecords("STUDENT", profile.getId()).size());
        return vo;
    }

    private String buildRecommendationReason(MatchScore match) {
        Map<String, BigDecimal> scoreMap = new LinkedHashMap<>();
        scoreMap.put("skill", match.getSkillScore());
        scoreMap.put("salary", match.getSalaryScore());
        scoreMap.put("location", match.getLocationScore());
        scoreMap.put("schedule", match.getScheduleScore());

        return scoreMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(entry -> "Top strength: " + entry.getKey())
                .orElse("Balanced recommendation");
    }
}
