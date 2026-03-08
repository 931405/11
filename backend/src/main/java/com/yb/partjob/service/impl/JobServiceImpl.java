package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.*;
import com.yb.partjob.model.vo.ApplicationVO;
import com.yb.partjob.model.vo.JobVO;
import com.yb.partjob.repository.*;
import com.yb.partjob.service.IJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
    private com.yb.partjob.repository.ChatMessageRepository chatMessageRepository;

    @Override
    public Page<JobVO> searchJobs(String keyword, Long categoryId, String location, String jobType, Long enterpriseId,
            int page, int size, Long currentUserId) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobPosition> jobPage = jobRepository.searchJobs(keyword, categoryId, location, jobType, enterpriseId,
                pageRequest);

        Long studentId = null;
        if (currentUserId != null) {
            StudentProfile profile = studentProfileRepository.findByUserId(currentUserId).orElse(null);
            if (profile != null) {
                studentId = profile.getId();
            }
        }

        final Long finalStudentId = studentId;
        List<JobVO> voList = jobPage.getContent().stream()
                .map(job -> convertToVO(job, finalStudentId))
                .collect(Collectors.toList());

        return new PageImpl<>(voList, pageRequest, jobPage.getTotalElements());
    }

    @Override
    @Transactional
    public JobVO getJobDetail(Long jobId, Long currentUserId) {
        JobPosition job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found"));

        // Increment view count
        job.setViewCount(job.getViewCount() + 1);
        jobRepository.save(job);

        Long studentId = null;
        if (currentUserId != null) {
            StudentProfile profile = studentProfileRepository.findByUserId(currentUserId).orElse(null);
            if (profile != null) {
                studentId = profile.getId();
            }
        }

        return convertToVO(job, studentId);
    }

    @Override
    @Transactional
    public JobApplication applyJob(Long userId, Long jobId, String message) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("请先完善个人简历后再投递"));

        JobPosition job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found"));

        if (!"OPEN".equals(job.getStatus())) {
            throw new BusinessException("Job is no longer accepting applications");
        }

        if (applicationRepository.existsByStudentIdAndJobId(profile.getId(), jobId)) {
            throw new BusinessException("You have already applied for this job");
        }

        JobApplication application = JobApplication.builder()
                .studentId(profile.getId())
                .jobId(jobId)
                .status("APPLIED")
                .applyMessage(message)
                .build();

        applicationRepository.save(application);

        // Update apply count
        job.setApplyCount(job.getApplyCount() + 1);
        jobRepository.save(job);

        return application;
    }

    @Override
    public Page<ApplicationVO> getMyApplications(Long userId, int page, int size) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return Page.empty();
        }

        Page<JobApplication> apps = applicationRepository.findByStudentId(profile.getId(),
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return apps.map(app -> {
            ApplicationVO vo = new ApplicationVO();
            vo.setId(app.getId());
            vo.setStudentId(app.getStudentId());
            vo.setJobId(app.getJobId());
            vo.setStatus(app.getStatus());
            vo.setApplyMessage(app.getApplyMessage());
            vo.setEnterpriseRemark(app.getEnterpriseRemark());
            vo.setCreatedAt(app.getCreatedAt());
            vo.setUpdatedAt(app.getUpdatedAt());

            // Enrich with job info
            jobRepository.findById(app.getJobId()).ifPresent(job -> {
                vo.setJobTitle(job.getTitle());
                vo.setSalaryMin(job.getSalaryMin());
                vo.setSalaryMax(job.getSalaryMax());
                vo.setWorkLocation(job.getWorkLocation());

                // Enrich with company info
                enterpriseInfoRepository.findById(job.getEnterpriseId()).ifPresent(e -> {
                    vo.setCompanyName(e.getCompanyName());
                    vo.setCompanyIndustry(e.getIndustry());
                });
            });

            // Build timeline events based on status
            vo.setTimeline(buildTimeline(app));

            // Fetch unread messages
            long unreadCount = chatMessageRepository.countByApplicationIdAndSenderRoleNotAndIsReadFalse(app.getId(),
                    "STUDENT");
            vo.setUnreadCount(unreadCount);

            return vo;
        });
    }

    private List<ApplicationVO.TimelineEvent> buildTimeline(JobApplication app) {
        List<ApplicationVO.TimelineEvent> events = new ArrayList<>();

        // Event 1: Applied (always present)
        ApplicationVO.TimelineEvent applied = new ApplicationVO.TimelineEvent();
        applied.setLabel("投递成功");
        applied.setDescription("您已成功投递该职位");
        applied.setTime(app.getCreatedAt());
        applied.setType("primary");
        events.add(applied);

        String status = app.getStatus();
        if ("APPLIED".equals(status))
            return events;

        // Event 2: Reviewing
        ApplicationVO.TimelineEvent reviewing = new ApplicationVO.TimelineEvent();
        reviewing.setLabel("HR已查阅");
        reviewing.setDescription("您的简历已被HR查阅");
        reviewing.setTime(app.getUpdatedAt());
        reviewing.setType("info");
        events.add(reviewing);
        if ("REVIEWING".equals(status))
            return events;

        // Event 3: Interview or Rejected
        if ("INTERVIEW".equals(status)) {
            ApplicationVO.TimelineEvent interview = new ApplicationVO.TimelineEvent();
            interview.setLabel("面试邀请");
            interview.setDescription(app.getEnterpriseRemark() != null ? app.getEnterpriseRemark() : "企业邀请您参加面试");
            interview.setTime(app.getUpdatedAt());
            interview.setType("warning");
            events.add(interview);
        } else if ("REJECTED".equals(status)) {
            ApplicationVO.TimelineEvent rejected = new ApplicationVO.TimelineEvent();
            rejected.setLabel("未通过");
            rejected.setDescription(app.getEnterpriseRemark() != null ? app.getEnterpriseRemark() : "很遗憾，您的申请未通过");
            rejected.setTime(app.getUpdatedAt());
            rejected.setType("danger");
            events.add(rejected);
        } else if ("ACCEPTED".equals(status)) {
            // Include interview step
            ApplicationVO.TimelineEvent interview = new ApplicationVO.TimelineEvent();
            interview.setLabel("面试通过");
            interview.setDescription("恭喜！面试环节已通过");
            interview.setTime(app.getUpdatedAt());
            interview.setType("warning");
            events.add(interview);

            ApplicationVO.TimelineEvent accepted = new ApplicationVO.TimelineEvent();
            accepted.setLabel("已录用");
            accepted.setDescription(app.getEnterpriseRemark() != null ? app.getEnterpriseRemark() : "恭喜！您已被录用");
            accepted.setTime(app.getUpdatedAt());
            accepted.setType("success");
            events.add(accepted);
        }

        return events;
    }

    @Override
    @Transactional
    public void favoriteJob(Long userId, Long jobId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("请先完善个人简历后再收藏"));

        if (!jobRepository.existsById(jobId)) {
            throw new BusinessException("Job not found");
        }

        if (favoriteRepository.existsByStudentIdAndJobId(profile.getId(), jobId)) {
            throw new BusinessException("Job already in favorites");
        }

        JobFavorite favorite = JobFavorite.builder()
                .studentId(profile.getId())
                .jobId(jobId)
                .build();
        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void unfavoriteJob(Long userId, Long jobId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (profile != null) {
            favoriteRepository.deleteByStudentIdAndJobId(profile.getId(), jobId);
        }
    }

    @Override
    public Page<JobVO> getMyFavorites(Long userId, int page, int size) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return Page.empty();
        }

        Page<JobFavorite> favPage = favoriteRepository.findByStudentId(profile.getId(),
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<Long> jobIds = favPage.getContent().stream()
                .map(JobFavorite::getJobId)
                .collect(Collectors.toList());

        List<JobPosition> jobs = jobRepository.findByIdIn(jobIds);

        List<JobVO> voList = jobs.stream()
                .map(job -> convertToVO(job, profile.getId()))
                .collect(Collectors.toList());

        return new PageImpl<>(voList, PageRequest.of(page - 1, size), favPage.getTotalElements());
    }

    @Override
    public List<JobCategory> getCategoryTree() {
        return categoryRepository.findByStatusOrderBySortOrder(1);
    }

    private JobVO convertToVO(JobPosition job, Long studentId) {
        JobVO vo = new JobVO();
        vo.setId(job.getId());
        vo.setTitle(job.getTitle());
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

        // Enrich with enterprise details
        enterpriseInfoRepository.findById(job.getEnterpriseId())
                .ifPresent(e -> {
                    vo.setCompanyName(e.getCompanyName());
                    vo.setCompanyScale(e.getCompanySize());
                    vo.setCompanyIndustry(e.getIndustry());
                });

        // Enrich with category name
        if (job.getCategoryId() != null) {
            categoryRepository.findById(job.getCategoryId())
                    .ifPresent(c -> vo.setCategoryName(c.getName()));
        }

        // Enrich with student-specific data
        if (studentId != null) {
            vo.setIsFavorited(favoriteRepository.existsByStudentIdAndJobId(studentId, job.getId()));
            vo.setIsApplied(applicationRepository.existsByStudentIdAndJobId(studentId, job.getId()));

            matchScoreRepository.findByStudentIdAndJobId(studentId, job.getId())
                    .ifPresent(ms -> vo.setMatchScore(ms.getTotalScore()));
        }

        return vo;
    }
}
