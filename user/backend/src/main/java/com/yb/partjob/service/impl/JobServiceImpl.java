package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.JobApplication;
import com.yb.partjob.model.JobCategory;
import com.yb.partjob.model.JobFavorite;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.vo.ApplicationVO;
import com.yb.partjob.model.vo.JobVO;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.JobApplicationRepository;
import com.yb.partjob.repository.JobCategoryRepository;
import com.yb.partjob.repository.JobFavoriteRepository;
import com.yb.partjob.repository.JobPositionRepository;
import com.yb.partjob.repository.MatchScoreRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.service.IJobService;
import com.yb.partjob.service.IMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements IJobService {

    @Autowired
    private JobPositionRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private JobFavoriteRepository favoriteRepository;

    @Autowired
    private JobCategoryRepository categoryRepository;

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private MatchScoreRepository matchScoreRepository;

    @Autowired
    private IMatchService matchService;

    @Override
    public Page<JobVO> searchJobs(String keyword, Long categoryId, String location, String jobType, Long enterpriseId,
            int page, int size, Long currentUserId) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 1) - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        String safeKeyword = blankToNull(keyword);
        String safeLocation = blankToNull(location);
        String safeJobType = blankToNull(jobType);

        StudentProfile profile = currentUserId == null
                ? null
                : studentProfileRepository.findByUserId(currentUserId).orElse(null);
        if (profile != null && hasComparableSignals(profile)) {
            matchService.computeMatchScores(profile.getId());
        }

        Page<JobPosition> jobs = jobRepository.searchJobs(safeKeyword, categoryId, safeLocation, safeJobType,
                enterpriseId, pageRequest);
        Long studentId = profile == null ? null : profile.getId();
        boolean includeMatch = profile != null && hasComparableSignals(profile);
        return jobs.map(job -> convertToVO(job, studentId, includeMatch));
    }

    @Override
    @Transactional
    public JobVO getJobDetail(Long jobId, Long currentUserId) {
        JobPosition job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("岗位不存在"));

        job.setViewCount((job.getViewCount() == null ? 0 : job.getViewCount()) + 1);
        jobRepository.save(job);

        StudentProfile profile = currentUserId == null
                ? null
                : studentProfileRepository.findByUserId(currentUserId).orElse(null);
        if (profile != null && hasComparableSignals(profile)) {
            matchService.computeMatchScores(profile.getId());
        }

        return convertToVO(job, profile == null ? null : profile.getId(),
                profile != null && hasComparableSignals(profile));
    }

    @Override
    @Transactional
    public JobApplication applyJob(Long userId, Long jobId, String message) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("请先完善个人资料后再投递"));
        JobPosition job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("岗位不存在"));

        if (!"OPEN".equals(job.getStatus())) {
            throw new BusinessException("当前岗位已停止投递");
        }
        if (applicationRepository.existsByStudentIdAndJobId(profile.getId(), jobId)) {
            throw new BusinessException("你已经投递过该岗位");
        }

        JobApplication application = JobApplication.builder()
                .studentId(profile.getId())
                .jobId(jobId)
                .status("APPLIED")
                .applyMessage(message)
                .build();
        JobApplication saved = applicationRepository.save(application);

        job.setApplyCount((job.getApplyCount() == null ? 0 : job.getApplyCount()) + 1);
        jobRepository.save(job);
        return saved;
    }

    @Override
    public Page<ApplicationVO> getMyApplications(Long userId, int page, int size) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return Page.empty();
        }

        Page<JobApplication> applications = applicationRepository.findByStudentId(profile.getId(),
                PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return applications.map(this::convertApplication);
    }

    @Override
    @Transactional
    public void favoriteJob(Long userId, Long jobId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("请先完善个人资料后再收藏岗位"));
        if (!jobRepository.existsById(jobId)) {
            throw new BusinessException("岗位不存在");
        }
        if (favoriteRepository.existsByStudentIdAndJobId(profile.getId(), jobId)) {
            throw new BusinessException("该岗位已收藏");
        }

        favoriteRepository.save(JobFavorite.builder()
                .studentId(profile.getId())
                .jobId(jobId)
                .build());
    }

    @Override
    @Transactional
    public void unfavoriteJob(Long userId, Long jobId) {
        studentProfileRepository.findByUserId(userId)
                .ifPresent(profile -> favoriteRepository.deleteByStudentIdAndJobId(profile.getId(), jobId));
    }

    @Override
    public Page<JobVO> getMyFavorites(Long userId, int page, int size) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return Page.empty();
        }
        if (hasComparableSignals(profile)) {
            matchService.computeMatchScores(profile.getId());
        }

        Page<JobFavorite> favoritePage = favoriteRepository.findByStudentId(profile.getId(),
                PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Long> jobIds = favoritePage.getContent().stream().map(JobFavorite::getJobId).toList();
        if (jobIds.isEmpty()) {
            return new PageImpl<>(List.of(), favoritePage.getPageable(), favoritePage.getTotalElements());
        }

        Map<Long, JobPosition> jobMap = jobRepository.findByIdIn(jobIds).stream()
                .collect(Collectors.toMap(JobPosition::getId, job -> job));
        List<JobVO> content = new ArrayList<>();
        for (Long jobId : jobIds) {
            JobPosition job = jobMap.get(jobId);
            if (job != null) {
                content.add(convertToVO(job, profile.getId(), hasComparableSignals(profile)));
            }
        }
        return new PageImpl<>(content, favoritePage.getPageable(), favoritePage.getTotalElements());
    }

    @Override
    public List<JobCategory> getCategoryTree() {
        return categoryRepository.findByStatusOrderBySortOrder(1);
    }

    private ApplicationVO convertApplication(JobApplication application) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(application.getId());
        vo.setStudentId(application.getStudentId());
        vo.setJobId(application.getJobId());
        vo.setStatus(application.getStatus());
        vo.setApplyMessage(application.getApplyMessage());
        vo.setEnterpriseRemark(application.getEnterpriseRemark());
        vo.setCreatedAt(application.getCreatedAt());
        vo.setUpdatedAt(application.getUpdatedAt());
        vo.setUnreadCount(0L);
        vo.setTimeline(buildTimeline(application));

        jobRepository.findById(application.getJobId()).ifPresent(job -> {
            vo.setJobTitle(job.getTitle());
            vo.setSalaryMin(job.getSalaryMin());
            vo.setSalaryMax(job.getSalaryMax());
            vo.setWorkLocation(job.getWorkLocation());
            enterpriseInfoRepository.findById(job.getEnterpriseId()).ifPresent(info -> {
                vo.setCompanyName(info.getCompanyName());
                vo.setCompanyIndustry(info.getIndustry());
            });
        });
        return vo;
    }

    private List<ApplicationVO.TimelineEvent> buildTimeline(JobApplication application) {
        List<ApplicationVO.TimelineEvent> timeline = new ArrayList<>();
        timeline.add(timelineEvent("已投递", "你的申请已提交给企业", application.getCreatedAt(), "primary"));

        if ("REVIEWING".equals(application.getStatus()) || "INTERVIEW".equals(application.getStatus())
                || "ACCEPTED".equals(application.getStatus()) || "REJECTED".equals(application.getStatus())) {
            timeline.add(timelineEvent("已查看", "企业已开始查看你的简历", application.getUpdatedAt(), "warning"));
        }
        if ("INTERVIEW".equals(application.getStatus())) {
            timeline.add(timelineEvent("待面试",
                    application.getEnterpriseRemark() != null ? application.getEnterpriseRemark() : "请留意面试安排",
                    application.getUpdatedAt(), "primary"));
        }
        if ("ACCEPTED".equals(application.getStatus())) {
            timeline.add(timelineEvent("已录用",
                    application.getEnterpriseRemark() != null ? application.getEnterpriseRemark() : "恭喜，你已通过录用",
                    application.getUpdatedAt(), "success"));
        }
        if ("REJECTED".equals(application.getStatus())) {
            timeline.add(timelineEvent("未通过",
                    application.getEnterpriseRemark() != null ? application.getEnterpriseRemark() : "很遗憾，本次申请未通过",
                    application.getUpdatedAt(), "danger"));
        }
        return timeline;
    }

    private ApplicationVO.TimelineEvent timelineEvent(String label, String description,
            java.time.LocalDateTime time, String type) {
        ApplicationVO.TimelineEvent event = new ApplicationVO.TimelineEvent();
        event.setLabel(label);
        event.setDescription(description);
        event.setTime(time);
        event.setType(type);
        return event;
    }

    private JobVO convertToVO(JobPosition job, Long studentId, boolean includeMatch) {
        JobVO vo = new JobVO();
        vo.setId(job.getId());
        vo.setTitle(job.getTitle());
        vo.setEnterpriseId(job.getEnterpriseId());
        vo.setJobType(job.getJobType());
        vo.setDescription(job.getDescription());
        vo.setRequirements(job.getRequirements());
        vo.setSkillsRequired(job.getSkillsRequired());
        vo.setSalaryMin(job.getSalaryMin());
        vo.setSalaryMax(job.getSalaryMax());
        vo.setWorkLocation(job.getWorkLocation());
        vo.setWorkSchedule(job.getWorkSchedule());
        vo.setEducationRequirement(job.getEducationRequirement());
        vo.setDurationRequirement(job.getDurationRequirement());
        vo.setJobTags(job.getJobTags());
        vo.setHeadcount(job.getHeadcount());
        vo.setStatus(job.getStatus());
        vo.setViewCount(job.getViewCount());
        vo.setApplyCount(job.getApplyCount());
        vo.setCreatedAt(job.getCreatedAt());

        enterpriseInfoRepository.findById(job.getEnterpriseId()).ifPresent(enterprise -> {
            vo.setCompanyName(enterprise.getCompanyName());
            vo.setCompanyScale(enterprise.getCompanySize());
            vo.setCompanyIndustry(enterprise.getIndustry());
        });
        if (job.getCategoryId() != null) {
            categoryRepository.findById(job.getCategoryId()).ifPresent(category -> vo.setCategoryName(category.getName()));
        }

        if (studentId != null) {
            vo.setIsFavorited(favoriteRepository.existsByStudentIdAndJobId(studentId, job.getId()));
            vo.setIsApplied(applicationRepository.existsByStudentIdAndJobId(studentId, job.getId()));
            if (includeMatch) {
                matchScoreRepository.findByStudentIdAndJobId(studentId, job.getId())
                        .ifPresent(score -> vo.setMatchScore(score.getTotalScore()));
            }
        } else {
            vo.setIsFavorited(false);
            vo.setIsApplied(false);
        }
        return vo;
    }

    private boolean hasComparableSignals(StudentProfile profile) {
        return (profile.getSkills() != null && !profile.getSkills().isBlank() && !"[]".equals(profile.getSkills().trim()))
                || profile.getExpectedSalaryMin() != null
                || profile.getExpectedSalaryMax() != null
                || (profile.getExpectedLocation() != null && !profile.getExpectedLocation().isBlank())
                || (profile.getAvailableSchedule() != null && !profile.getAvailableSchedule().isBlank());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
