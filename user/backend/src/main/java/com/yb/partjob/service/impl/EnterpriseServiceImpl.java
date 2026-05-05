package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.InterviewInvitation;
import com.yb.partjob.model.JobApplication;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.MatchScore;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.InterviewInvitationDTO;
import com.yb.partjob.model.dto.InviteDTO;
import com.yb.partjob.model.dto.JobPositionDTO;
import com.yb.partjob.model.vo.CandidateVO;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.model.vo.TrendVO;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.InterviewInvitationRepository;
import com.yb.partjob.repository.JobApplicationRepository;
import com.yb.partjob.repository.JobPositionRepository;
import com.yb.partjob.repository.MatchScoreRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IEnterpriseService;
import com.yb.partjob.service.IMatchService;
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
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private MatchScoreRepository matchScoreRepository;

    @Autowired
    private InterviewInvitationRepository interviewInvitationRepository;

    @Autowired
    private IMatchService matchService;

    @Override
    public EnterpriseInfo getEnterpriseInfo(Long userId) {
        return enterpriseInfoRepository.findByUserId(userId)
                .orElseGet(() -> enterpriseInfoRepository.save(EnterpriseInfo.builder()
                        .userId(userId)
                        .companyName("未命名企业")
                        .certificationStatus("PENDING")
                        .build()));
    }

    @Override
    @Transactional
    public EnterpriseInfo updateEnterpriseInfo(Long userId, EnterpriseInfo info) {
        EnterpriseInfo existing = getEnterpriseInfo(userId);

        if (info.getCompanyName() != null && !info.getCompanyName().isBlank()) {
            existing.setCompanyName(info.getCompanyName().trim());
        }
        if (info.getIndustry() != null) {
            existing.setIndustry(info.getIndustry().trim());
        }
        if (info.getCompanySize() != null) {
            existing.setCompanySize(info.getCompanySize().trim());
        }
        if (info.getContactPerson() != null) {
            existing.setContactPerson(info.getContactPerson().trim());
        }
        if (info.getContactPhone() != null) {
            existing.setContactPhone(info.getContactPhone().trim());
        }
        if (info.getCompanyAddress() != null) {
            existing.setCompanyAddress(info.getCompanyAddress().trim());
        }
        if (info.getBusinessLicense() != null) {
            existing.setBusinessLicense(info.getBusinessLicense().trim());
        }
        if (info.getDescription() != null) {
            existing.setDescription(info.getDescription().trim());
        }

        return enterpriseInfoRepository.save(existing);
    }

    @Override
    @Transactional
    public JobPosition createJob(Long userId, JobPositionDTO dto) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BusinessException("岗位名称不能为空");
        }

        JobPosition job = JobPosition.builder()
                .enterpriseId(enterprise.getId())
                .title(dto.getTitle().trim())
                .jobType(trim(dto.getJobType()))
                .categoryId(dto.getCategoryId())
                .description(trim(dto.getDescription()))
                .requirements(trim(dto.getRequirements()))
                .skillsRequired(trim(dto.getSkillsRequired()))
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .workLocation(trim(dto.getWorkLocation()))
                .workSchedule(trim(dto.getWorkSchedule()))
                .headcount(dto.getHeadcount() != null ? dto.getHeadcount() : 1)
                .status(dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus() : "OPEN")
                .build();

        JobPosition saved = jobPositionRepository.save(job);
        recomputeAllStudentMatches();
        return saved;
    }

    @Override
    @Transactional
    public JobPosition updateJob(Long userId, Long jobId, JobPositionDTO dto) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        JobPosition job = requireOwnedJob(enterprise, jobId);

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            job.setTitle(dto.getTitle().trim());
        }
        if (dto.getJobType() != null) {
            job.setJobType(trim(dto.getJobType()));
        }
        if (dto.getCategoryId() != null) {
            job.setCategoryId(dto.getCategoryId());
        }
        if (dto.getDescription() != null) {
            job.setDescription(trim(dto.getDescription()));
        }
        if (dto.getRequirements() != null) {
            job.setRequirements(trim(dto.getRequirements()));
        }
        if (dto.getSkillsRequired() != null) {
            job.setSkillsRequired(trim(dto.getSkillsRequired()));
        }
        if (dto.getSalaryMin() != null) {
            job.setSalaryMin(dto.getSalaryMin());
        }
        if (dto.getSalaryMax() != null) {
            job.setSalaryMax(dto.getSalaryMax());
        }
        if (dto.getWorkLocation() != null) {
            job.setWorkLocation(trim(dto.getWorkLocation()));
        }
        if (dto.getWorkSchedule() != null) {
            job.setWorkSchedule(trim(dto.getWorkSchedule()));
        }
        if (dto.getHeadcount() != null) {
            job.setHeadcount(dto.getHeadcount());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            job.setStatus(dto.getStatus());
        }

        JobPosition saved = jobPositionRepository.save(job);
        recomputeAllStudentMatches();
        return saved;
    }

    @Override
    @Transactional
    public void deleteJob(Long userId, Long jobId) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        JobPosition job = requireOwnedJob(enterprise, jobId);
        jobPositionRepository.delete(job);
        matchScoreRepository.deleteByJobId(jobId);
    }

    @Override
    public Page<JobPosition> getEnterpriseJobs(Long userId, String status, int page, int size) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        PageRequest pageRequest = PageRequest.of(Math.max(page, 1) - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isBlank()) {
            return jobPositionRepository.findByEnterpriseIdAndStatus(enterprise.getId(), status, pageRequest);
        }
        return jobPositionRepository.findByEnterpriseId(enterprise.getId(), pageRequest);
    }

    @Override
    public Page<CandidateVO> getJobCandidates(Long userId, Long jobId, int page, int size) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        JobPosition job = requireOwnedJob(enterprise, jobId);
        Page<JobApplication> applications = applicationRepository.findByJobId(jobId,
                PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return applications.map(application -> buildCandidateVO(resolveStudentProfileByProfileId(application.getStudentId()),
                application, job.getId(), true));
    }

    @Override
    public StudentProfile getStudentProfile(Long studentUserId) {
        return studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new BusinessException("学生简历不存在"));
    }

    @Override
    public Page<CandidateVO> searchTalents(String keyword, int page, int size) {
        Page<StudentProfile> profiles = studentProfileRepository.searchPublicProfiles(
                keyword == null || keyword.isBlank() ? null : keyword.trim(),
                PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "updatedAt")));
        return profiles.map(profile -> buildCandidateVO(profile, null, null, false));
    }

    @Override
    @Transactional
    public Long inviteTalent(Long enterpriseUserId, InviteDTO dto) {
        EnterpriseInfo enterprise = requireEnterprise(enterpriseUserId);
        requireOwnedJob(enterprise, dto.getJobId());

        StudentProfile studentProfile = studentProfileRepository.findByUserId(dto.getStudentId())
                .orElseGet(() -> studentProfileRepository.findById(dto.getStudentId()).orElse(null));
        if (studentProfile == null) {
            throw new BusinessException("学生简历不存在");
        }
        if (applicationRepository.existsByStudentIdAndJobId(studentProfile.getId(), dto.getJobId())) {
            throw new BusinessException("该学生已与该岗位建立申请记录");
        }

        JobApplication application = JobApplication.builder()
                .studentId(studentProfile.getId())
                .jobId(dto.getJobId())
                .status("REVIEWING")
                .applyMessage(trim(dto.getGreeting()))
                .enterpriseRemark("企业主动邀约")
                .build();

        JobApplication saved = applicationRepository.save(application);
        jobPositionRepository.findById(dto.getJobId()).ifPresent(job -> {
            job.setApplyCount((job.getApplyCount() == null ? 0 : job.getApplyCount()) + 1);
            jobPositionRepository.save(job);
        });
        return saved.getId();
    }

    @Override
    public TrendVO getDailyApplicationTrend(Long userId) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        List<JobPosition> jobs = jobPositionRepository.findByEnterpriseId(enterprise.getId(),
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (jobs.isEmpty()) {
            return emptyTrend();
        }

        List<Long> jobIds = jobs.stream().map(JobPosition::getId).toList();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0)
                .withNano(0);

        List<JobApplication> applications = applicationRepository.findByJobIdInAndCreatedAtAfter(jobIds, sevenDaysAgo);
        Map<String, Long> counts = applications.stream()
                .collect(Collectors.groupingBy(
                        application -> application.getCreatedAt().getMonthValue() + "/"
                                + application.getCreatedAt().getDayOfMonth(),
                        Collectors.counting()));

        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = LocalDateTime.now().minusDays(i);
            String label = day.getMonthValue() + "/" + day.getDayOfMonth();
            dates.add(label);
            values.add(counts.getOrDefault(label, 0L).intValue());
        }
        return new TrendVO(dates, values);
    }

    @Override
    @Transactional
    public void updateApplicationStatus(Long userId, Long applicationId, String status, String remark) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申请记录不存在"));
        requireOwnedJob(enterprise, application.getJobId());

        application.setStatus(status);
        if (remark != null) {
            application.setEnterpriseRemark(remark.trim());
        }
        applicationRepository.save(application);
    }

    @Override
    @Transactional
    public void sendInterviewInvitation(Long enterpriseUserId, InterviewInvitationDTO dto) {
        EnterpriseInfo enterprise = requireEnterprise(enterpriseUserId);
        JobApplication application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new BusinessException("申请记录不存在"));
        JobPosition job = requireOwnedJob(enterprise, application.getJobId());

        if (interviewInvitationRepository.existsByApplicationId(application.getId())) {
            throw new BusinessException("该申请已经发送过面试邀请");
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

        application.setStatus("INTERVIEW");
        application.setEnterpriseRemark("已发送面试邀请");
        applicationRepository.save(application);
    }

    @Override
    public Page<InterviewInvitationVO> getEnterpriseInvitations(Long enterpriseUserId, int page, int size) {
        EnterpriseInfo enterprise = requireEnterprise(enterpriseUserId);
        Page<InterviewInvitation> invitations = interviewInvitationRepository.findByEnterpriseId(enterprise.getId(),
                PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return invitations.map(invitation -> {
            InterviewInvitationVO vo = new InterviewInvitationVO();
            BeanUtils.copyProperties(invitation, vo);
            vo.setCompanyName(enterprise.getCompanyName());
            jobPositionRepository.findById(invitation.getJobId()).ifPresent(job -> vo.setJobTitle(job.getTitle()));
            studentProfileRepository.findById(invitation.getStudentId())
                    .flatMap(profile -> sysUserRepository.findById(profile.getUserId()))
                    .ifPresent(user -> vo.setStudentName(user.getRealName()));
            return vo;
        });
    }

    @Override
    public Page<CandidateVO> getJobMatches(Long userId, Long jobId, int page, int size) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        requireOwnedJob(enterprise, jobId);
        recomputeAllStudentMatches();

        Page<MatchScore> matches = matchScoreRepository.findByJobIdOrderByTotalScoreDesc(jobId,
                PageRequest.of(Math.max(page, 1) - 1, size));
        return matches.map(match -> buildCandidateVO(resolveStudentProfileByProfileId(match.getStudentId()), null,
                jobId, true));
    }

    @Override
    public Map<String, Object> getEnterpriseAnalytics(Long userId) {
        EnterpriseInfo enterprise = requireEnterprise(userId);
        List<JobPosition> jobs = jobPositionRepository.findByEnterpriseId(enterprise.getId(),
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<Long> jobIds = jobs.stream().map(JobPosition::getId).toList();

        long activeJobs = jobs.stream().filter(job -> "OPEN".equals(job.getStatus())).count();
        long closedJobs = jobs.size() - activeJobs;
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
        for (Long currentJobId : jobIds) {
            sampleScores.addAll(matchScoreRepository.findByJobIdOrderByTotalScoreDesc(currentJobId, PageRequest.of(0, 20))
                    .getContent());
        }
        if (!sampleScores.isEmpty()) {
            BigDecimal total = sampleScores.stream()
                    .map(MatchScore::getTotalScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageMatchScore = total.divide(BigDecimal.valueOf(sampleScores.size()), 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("activeJobs", activeJobs);
        analytics.put("closedJobs", closedJobs);
        analytics.put("totalApplications", totalApplications);
        analytics.put("pendingApplications", pendingApplications);
        analytics.put("interviewingApplications", interviewingApplications);
        analytics.put("matchedTalents", matchedTalents);
        analytics.put("averageMatchScore", averageMatchScore);
        analytics.put("trend", getDailyApplicationTrend(userId));
        return analytics;
    }

    private TrendVO emptyTrend() {
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = LocalDateTime.now().minusDays(i);
            dates.add(day.getMonthValue() + "/" + day.getDayOfMonth());
            values.add(0);
        }
        return new TrendVO(dates, values);
    }

    private EnterpriseInfo requireEnterprise(Long userId) {
        return getEnterpriseInfo(userId);
    }

    private JobPosition requireOwnedJob(EnterpriseInfo enterprise, Long jobId) {
        JobPosition job = jobPositionRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("岗位不存在"));
        if (!job.getEnterpriseId().equals(enterprise.getId())) {
            throw new BusinessException("无权操作该岗位");
        }
        return job;
    }

    private StudentProfile resolveStudentProfileByProfileId(Long profileId) {
        return studentProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("学生简历不存在"));
    }

    private CandidateVO buildCandidateVO(StudentProfile profile, JobApplication application, Long jobId,
            boolean includeMatch) {
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

        vo.setIntegratedSourceCount(0);
        return vo;
    }

    private String buildRecommendationReason(MatchScore match) {
        Map<String, BigDecimal> scores = new LinkedHashMap<>();
        scores.put("技能契合度", match.getSkillScore());
        scores.put("薪资契合度", match.getSalaryScore());
        scores.put("地点契合度", match.getLocationScore());
        scores.put("时间契合度", match.getScheduleScore());

        return scores.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(entry -> "该候选人在" + entry.getKey() + "方面表现更优")
                .orElse("综合匹配表现良好");
    }

    private void recomputeAllStudentMatches() {
        studentProfileRepository.findAll().forEach(profile -> matchService.computeMatchScores(profile.getId()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
