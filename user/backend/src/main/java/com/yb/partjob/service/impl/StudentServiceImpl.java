package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.InterviewInvitation;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.MatchScore;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.ResumeParseResultDTO;
import com.yb.partjob.model.dto.StudentProfileDTO;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.model.vo.JobVO;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.InterviewInvitationRepository;
import com.yb.partjob.repository.JobApplicationRepository;
import com.yb.partjob.repository.JobCategoryRepository;
import com.yb.partjob.repository.JobFavoriteRepository;
import com.yb.partjob.repository.JobPositionRepository;
import com.yb.partjob.repository.MatchScoreRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IMatchService;
import com.yb.partjob.service.IStudentService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StudentServiceImpl implements IStudentService {

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Autowired
    private JobCategoryRepository categoryRepository;

    @Autowired
    private JobFavoriteRepository favoriteRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private MatchScoreRepository matchScoreRepository;

    @Autowired
    private InterviewInvitationRepository interviewInvitationRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private IMatchService matchService;

    @Override
    public StudentProfile getProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseGet(() -> profileRepository.save(defaultProfile(userId)));
    }

    @Override
    @Transactional
    public StudentProfile updateProfile(Long userId, StudentProfileDTO dto) {
        StudentProfile profile = profileRepository.findByUserId(userId).orElseGet(() -> defaultProfile(userId));

        if (dto.getUniversity() != null) profile.setUniversity(dto.getUniversity());
        if (dto.getMajor() != null) profile.setMajor(dto.getMajor());
        if (dto.getEducationLevel() != null) profile.setEducationLevel(dto.getEducationLevel());
        if (dto.getEnrollmentYear() != null) profile.setEnrollmentYear(dto.getEnrollmentYear());
        if (dto.getSkills() != null) profile.setSkills(dto.getSkills());
        if (dto.getSelfIntro() != null) profile.setSelfIntro(dto.getSelfIntro());
        if (dto.getExpectedSalaryMin() != null) profile.setExpectedSalaryMin(dto.getExpectedSalaryMin());
        if (dto.getExpectedSalaryMax() != null) profile.setExpectedSalaryMax(dto.getExpectedSalaryMax());
        if (dto.getExpectedLocation() != null) profile.setExpectedLocation(dto.getExpectedLocation());
        if (dto.getAvailableSchedule() != null) profile.setAvailableSchedule(dto.getAvailableSchedule());
        if (dto.getResumeAttachments() != null) profile.setResumeAttachments(dto.getResumeAttachments());

        if (dto.getPrivacyResumeOpen() != null) profile.setPrivacyResumeOpen(dto.getPrivacyResumeOpen());
        if (dto.getPrivacyShowName() != null) profile.setPrivacyShowName(dto.getPrivacyShowName());
        if (dto.getPrivacyShowContact() != null) profile.setPrivacyShowContact(dto.getPrivacyShowContact());
        if (dto.getPrivacyShowOnline() != null) profile.setPrivacyShowOnline(dto.getPrivacyShowOnline());
        if (dto.getPrivacyShowLastActive() != null) profile.setPrivacyShowLastActive(dto.getPrivacyShowLastActive());

        if (dto.getNotifyNewJob() != null) profile.setNotifyNewJob(dto.getNotifyNewJob());
        if (dto.getNotifyApplicationStatus() != null) profile.setNotifyApplicationStatus(dto.getNotifyApplicationStatus());
        if (dto.getNotifyInterview() != null) profile.setNotifyInterview(dto.getNotifyInterview());
        if (dto.getNotifyNewMessage() != null) profile.setNotifyNewMessage(dto.getNotifyNewMessage());
        if (dto.getNotifyMessageSound() != null) profile.setNotifyMessageSound(dto.getNotifyMessageSound());
        if (dto.getNotifySystemAnnouncement() != null) profile.setNotifySystemAnnouncement(dto.getNotifySystemAnnouncement());
        if (dto.getNotifyPromotion() != null) profile.setNotifyPromotion(dto.getNotifyPromotion());

        StudentProfile saved = profileRepository.save(profile);
        matchService.computeMatchScores(saved.getId());
        return saved;
    }

    @Override
    public Page<JobVO> getRecommendedJobs(Long userId, int page, int size) {
        StudentProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile != null && hasComparableSignals(profile)) {
            matchService.computeMatchScores(profile.getId());
            Page<MatchScore> matches = matchScoreRepository.findByStudentIdOrderByTotalScoreDesc(profile.getId(),
                    PageRequest.of(Math.max(page, 1) - 1, size));
            if (matches.hasContent()) {
                return matches.map(match -> toJobVO(jobPositionRepository.findById(match.getJobId()).orElse(null), profile, match));
            }
        }

        Page<JobPosition> latestJobs = jobPositionRepository.findByStatus("OPEN",
                PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return latestJobs.map(job -> toJobVO(job, profile, null));
    }

    @Override
    public ResumeParseResultDTO parseResumePDF(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传 PDF 简历文件");
        }

        System.setProperty("pdfbox.fontcache", "");

        try (InputStream inputStream = file.getInputStream();
                RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputStream);
                PDDocument document = Loader.loadPDF(buffer)) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            if (text == null) {
                text = "";
            }

            String extension = ".pdf";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            Path uploadDir = Paths.get("uploads", "resumes");
            Files.createDirectories(uploadDir);
            String fileName = UUID.randomUUID() + extension;
            Path target = uploadDir.resolve(fileName);
            file.transferTo(target);

            return ResumeParseResultDTO.builder()
                    .university(extractUniversity(text))
                    .skills(extractSkills(text))
                    .selfIntro(extractSelfIntro(text))
                    .fileName(originalFilename)
                    .fileUrl("/uploads/resumes/" + fileName)
                    .build();
        } catch (Exception e) {
            throw new BusinessException("简历解析失败，请稍后重试");
        }
    }

    @Override
    public Page<InterviewInvitationVO> getMyInvitations(Long studentUserId, int page, int size) {
        StudentProfile profile = profileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new BusinessException("学生简历不存在"));
        Page<InterviewInvitation> invitations = interviewInvitationRepository.findByStudentId(profile.getId(),
                PageRequest.of(Math.max(page, 1) - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return invitations.map(invitation -> {
            InterviewInvitationVO vo = new InterviewInvitationVO();
            BeanUtils.copyProperties(invitation, vo);
            jobPositionRepository.findById(invitation.getJobId()).ifPresent(job -> vo.setJobTitle(job.getTitle()));
            enterpriseInfoRepository.findById(invitation.getEnterpriseId())
                    .ifPresent(info -> vo.setCompanyName(info.getCompanyName()));
            sysUserRepository.findById(profile.getUserId()).ifPresent(user -> vo.setStudentName(user.getRealName()));
            return vo;
        });
    }

    @Override
    @Transactional
    public void updateInvitationStatus(Long studentUserId, Long invitationId, String status) {
        StudentProfile profile = profileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new BusinessException("学生简历不存在"));
        InterviewInvitation invitation = interviewInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException("面试邀请不存在"));

        if (!invitation.getStudentId().equals(profile.getId())) {
            throw new BusinessException("无权操作该面试邀请");
        }
        invitation.setStatus(status);
        interviewInvitationRepository.save(invitation);
    }

    @Override
    public Map<String, Object> getDashboardSummary(Long userId) {
        StudentProfile profile = getProfile(userId);
        if (hasComparableSignals(profile)) {
            matchService.computeMatchScores(profile.getId());
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("profileCompleteness", computeProfileCompleteness(profile));
        summary.put("totalApplications", applicationRepository.countByStudentId(profile.getId()));
        summary.put("pendingApplications", applicationRepository.countByStudentIdAndStatus(profile.getId(), "APPLIED")
                + applicationRepository.countByStudentIdAndStatus(profile.getId(), "REVIEWING"));
        summary.put("totalFavorites", favoriteRepository.countByStudentId(profile.getId()));
        summary.put("pendingInterviews", interviewInvitationRepository.findByStudentId(profile.getId(),
                org.springframework.data.domain.Pageable.unpaged()).stream()
                .filter(invitation -> "PENDING".equals(invitation.getStatus()))
                .count());

        Page<MatchScore> matches = matchScoreRepository.findByStudentIdOrderByTotalScoreDesc(profile.getId(), PageRequest.of(0, 20));
        summary.put("recommendationCount", matches.getTotalElements());
        if (matches.isEmpty()) {
            summary.put("averageMatchScore", BigDecimal.ZERO);
        } else {
            BigDecimal total = matches.getContent().stream()
                    .map(MatchScore::getTotalScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            summary.put("averageMatchScore",
                    total.divide(BigDecimal.valueOf(matches.getContent().size()), 2, RoundingMode.HALF_UP));
        }
        return summary;
    }

    private StudentProfile defaultProfile(Long userId) {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(userId);
        profile.setPrivacyResumeOpen(true);
        profile.setPrivacyShowName(true);
        profile.setPrivacyShowContact(false);
        profile.setPrivacyShowOnline(true);
        profile.setPrivacyShowLastActive(true);
        profile.setNotifyNewJob(true);
        profile.setNotifyApplicationStatus(true);
        profile.setNotifyInterview(true);
        profile.setNotifyNewMessage(true);
        profile.setNotifyMessageSound(false);
        profile.setNotifySystemAnnouncement(true);
        profile.setNotifyPromotion(false);
        profile.setSkills("[]");
        profile.setResumeAttachments("[]");
        return profile;
    }

    private JobVO toJobVO(JobPosition job, StudentProfile profile, MatchScore match) {
        if (job == null) {
            return null;
        }

        JobVO vo = new JobVO();
        BeanUtils.copyProperties(job, vo);
        if (match != null) {
            vo.setMatchScore(match.getTotalScore());
        }

        enterpriseInfoRepository.findById(job.getEnterpriseId()).ifPresent(info -> {
            vo.setCompanyName(info.getCompanyName());
            vo.setCompanyScale(info.getCompanySize());
            vo.setCompanyIndustry(info.getIndustry());
        });
        if (job.getCategoryId() != null) {
            categoryRepository.findById(job.getCategoryId()).ifPresent(category -> vo.setCategoryName(category.getName()));
        }

        if (profile != null) {
            vo.setIsApplied(applicationRepository.existsByStudentIdAndJobId(profile.getId(), job.getId()));
            vo.setIsFavorited(favoriteRepository.existsByStudentIdAndJobId(profile.getId(), job.getId()));
        } else {
            vo.setIsApplied(false);
            vo.setIsFavorited(false);
        }
        return vo;
    }

    private BigDecimal computeProfileCompleteness(StudentProfile profile) {
        int total = 7;
        int filled = 0;
        if (profile.getUniversity() != null && !profile.getUniversity().isBlank()) filled++;
        if (profile.getMajor() != null && !profile.getMajor().isBlank()) filled++;
        if (profile.getEducationLevel() != null && !profile.getEducationLevel().isBlank()) filled++;
        if (profile.getSkills() != null && !profile.getSkills().isBlank() && !"[]".equals(profile.getSkills().trim())) filled++;
        if (profile.getExpectedLocation() != null && !profile.getExpectedLocation().isBlank()) filled++;
        if (profile.getAvailableSchedule() != null && !profile.getAvailableSchedule().isBlank()) filled++;
        if (profile.getSelfIntro() != null && !profile.getSelfIntro().isBlank()) filled++;
        return BigDecimal.valueOf(filled)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private boolean hasComparableSignals(StudentProfile profile) {
        return (profile.getSkills() != null && !profile.getSkills().isBlank() && !"[]".equals(profile.getSkills().trim()))
                || profile.getExpectedSalaryMin() != null
                || profile.getExpectedSalaryMax() != null
                || (profile.getExpectedLocation() != null && !profile.getExpectedLocation().isBlank())
                || (profile.getAvailableSchedule() != null && !profile.getAvailableSchedule().isBlank());
    }

    private String extractUniversity(String text) {
        String[] keywords = { "大学", "学院", "University", "College" };
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.length() > 40) {
                continue;
            }
            for (String keyword : keywords) {
                if (trimmed.contains(keyword)) {
                    return trimmed;
                }
            }
        }
        return "";
    }

    private List<String> extractSkills(String text) {
        String lower = text.toLowerCase();
        String[] candidates = { "java", "spring", "spring boot", "vue", "react", "mysql", "redis", "python",
                "javascript", "typescript", "linux", "git", "excel", "office", "ps", "figma" };
        List<String> skills = new ArrayList<>();
        for (String candidate : candidates) {
            if (lower.contains(candidate) && !skills.contains(candidate)) {
                skills.add(candidate);
            }
        }
        return skills;
    }

    private String extractSelfIntro(String text) {
        String[] keywords = { "自我评价", "个人总结", "自我介绍", "个人优势", "Summary", "Profile" };
        String[] stopKeywords = { "教育经历", "工作经历", "项目经历", "实习经历", "技能证书", "校园经历" };
        boolean capture = false;
        StringBuilder builder = new StringBuilder();

        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (!capture) {
                for (String keyword : keywords) {
                    if (trimmed.contains(keyword)) {
                        capture = true;
                        break;
                    }
                }
                continue;
            }

            boolean shouldStop = false;
            for (String stopKeyword : stopKeywords) {
                if (trimmed.contains(stopKeyword)) {
                    shouldStop = true;
                    break;
                }
            }
            if (shouldStop) {
                break;
            }

            builder.append(trimmed).append(' ');
            if (builder.length() >= 240) {
                break;
            }
        }
        return builder.toString().trim();
    }
}
