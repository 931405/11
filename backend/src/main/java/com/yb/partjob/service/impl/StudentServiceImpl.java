package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.StudentProfileDTO;
import com.yb.partjob.model.vo.JobVO;
import com.yb.partjob.repository.JobPositionRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.InterviewInvitationRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.model.InterviewInvitation;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.service.IMatchService;
import com.yb.partjob.service.IStudentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements IStudentService {

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private InterviewInvitationRepository interviewInvitationRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private IMatchService matchService;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Override
    public StudentProfile getProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    StudentProfile p = new StudentProfile();
                    p.setUserId(userId);
                    return profileRepository.save(p);
                });
    }

    @Override
    public StudentProfile updateProfile(Long userId, StudentProfileDTO dto) {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    StudentProfile p = new StudentProfile();
                    p.setUserId(userId);
                    return p;
                });

        if (dto.getUniversity() != null)
            profile.setUniversity(dto.getUniversity());
        if (dto.getMajor() != null)
            profile.setMajor(dto.getMajor());
        if (dto.getEducationLevel() != null)
            profile.setEducationLevel(dto.getEducationLevel());
        if (dto.getEnrollmentYear() != null)
            profile.setEnrollmentYear(dto.getEnrollmentYear());
        if (dto.getSkills() != null)
            profile.setSkills(dto.getSkills());
        if (dto.getSelfIntro() != null)
            profile.setSelfIntro(dto.getSelfIntro());
        if (dto.getExpectedSalaryMin() != null)
            profile.setExpectedSalaryMin(dto.getExpectedSalaryMin());
        if (dto.getExpectedSalaryMax() != null)
            profile.setExpectedSalaryMax(dto.getExpectedSalaryMax());
        if (dto.getExpectedLocation() != null)
            profile.setExpectedLocation(dto.getExpectedLocation());
        if (dto.getAvailableSchedule() != null)
            profile.setAvailableSchedule(dto.getAvailableSchedule());
        if (dto.getResumeAttachments() != null)
            profile.setResumeAttachments(dto.getResumeAttachments());

        // Privacy settings
        if (dto.getPrivacyResumeOpen() != null)
            profile.setPrivacyResumeOpen(dto.getPrivacyResumeOpen());
        if (dto.getPrivacyShowName() != null)
            profile.setPrivacyShowName(dto.getPrivacyShowName());
        if (dto.getPrivacyShowContact() != null)
            profile.setPrivacyShowContact(dto.getPrivacyShowContact());
        if (dto.getPrivacyShowOnline() != null)
            profile.setPrivacyShowOnline(dto.getPrivacyShowOnline());
        if (dto.getPrivacyShowLastActive() != null)
            profile.setPrivacyShowLastActive(dto.getPrivacyShowLastActive());

        // Notification settings
        if (dto.getNotifyNewJob() != null)
            profile.setNotifyNewJob(dto.getNotifyNewJob());
        if (dto.getNotifyApplicationStatus() != null)
            profile.setNotifyApplicationStatus(dto.getNotifyApplicationStatus());
        if (dto.getNotifyInterview() != null)
            profile.setNotifyInterview(dto.getNotifyInterview());
        if (dto.getNotifyNewMessage() != null)
            profile.setNotifyNewMessage(dto.getNotifyNewMessage());
        if (dto.getNotifyMessageSound() != null)
            profile.setNotifyMessageSound(dto.getNotifyMessageSound());
        if (dto.getNotifySystemAnnouncement() != null)
            profile.setNotifySystemAnnouncement(dto.getNotifySystemAnnouncement());
        if (dto.getNotifyPromotion() != null)
            profile.setNotifyPromotion(dto.getNotifyPromotion());

        StudentProfile saved = profileRepository.save(profile);

        // Recompute match scores after profile update
        matchService.computeMatchScores(profile.getId());

        return saved;
    }

    @Autowired
    private com.yb.partjob.repository.JobFavoriteRepository favoriteRepository;

    @Autowired
    private com.yb.partjob.repository.JobApplicationRepository applicationRepository;

    @Autowired
    private com.yb.partjob.repository.JobCategoryRepository categoryRepository;

    @Override
    public Page<JobVO> getRecommendedJobs(Long userId, int page, int size) {
        StudentProfile profile = profileRepository.findByUserId(userId).orElse(null);

        // If profile exists, try match-score based recommendations
        if (profile != null) {
            Page<com.yb.partjob.model.MatchScore> matches = matchService.getTopMatches(profile.getId(), page, size);
            if (matches.hasContent()) {
                return matches.map(score -> {
                    com.yb.partjob.model.JobPosition jp = jobPositionRepository.findById(score.getJobId()).orElse(null);
                    if (jp == null)
                        return null;

                    JobVO vo = new JobVO();
                    org.springframework.beans.BeanUtils.copyProperties(jp, vo);
                    vo.setMatchScore(score.getTotalScore());

                    enterpriseInfoRepository.findById(jp.getEnterpriseId())
                            .ifPresent(e -> vo.setCompanyName(e.getCompanyName()));

                    if (jp.getCategoryId() != null) {
                        categoryRepository.findById(jp.getCategoryId())
                                .ifPresent(c -> vo.setCategoryName(c.getName()));
                    }

                    boolean isApplied = applicationRepository.existsByStudentIdAndJobId(profile.getId(), jp.getId());
                    boolean isFavorited = favoriteRepository.existsByStudentIdAndJobId(profile.getId(), jp.getId());
                    vo.setIsApplied(isApplied);
                    vo.setIsFavorited(isFavorited);

                    return vo;
                });
            }
        }

        // Fallback: show latest open jobs when no profile or no match scores
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        Page<com.yb.partjob.model.JobPosition> latestJobs = jobPositionRepository.findByStatus("OPEN", pageable);

        return latestJobs.map(jp -> {
            JobVO vo = new JobVO();
            org.springframework.beans.BeanUtils.copyProperties(jp, vo);
            vo.setMatchScore(java.math.BigDecimal.ZERO); // No score if no profile

            enterpriseInfoRepository.findById(jp.getEnterpriseId())
                    .ifPresent(e -> vo.setCompanyName(e.getCompanyName()));

            if (jp.getCategoryId() != null) {
                categoryRepository.findById(jp.getCategoryId())
                        .ifPresent(c -> vo.setCategoryName(c.getName()));
            }

            if (profile != null) {
                boolean isApplied = applicationRepository.existsByStudentIdAndJobId(profile.getId(), jp.getId());
                boolean isFavorited = favoriteRepository.existsByStudentIdAndJobId(profile.getId(), jp.getId());
                vo.setIsApplied(isApplied);
                vo.setIsFavorited(isFavorited);
            }

            return vo;
        });
    }

    @Override
    public com.yb.partjob.model.dto.ResumeParseResultDTO parseResumePDF(
            org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Upload file is empty");
        }

        // Disable font caching on Windows to prevent EOFException in corrupted system
        // fonts
        System.setProperty("pdfbox.fontcache", "");

        try (java.io.InputStream is = file.getInputStream();
                org.apache.pdfbox.io.RandomAccessReadBuffer buffer = new org.apache.pdfbox.io.RandomAccessReadBuffer(
                        is);
                org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(buffer)) {

            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            // Since we only extract text, ignore font missing warnings
            stripper.setSortByPosition(true);

            String text = stripper.getText(document);
            if (text == null)
                text = "";

            String university = extractUniversity(text);
            java.util.List<String> skills = extractSkills(text);
            String selfIntro = extractSelfIntro(text);

            // Save the file to local disk
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                extension = ".pdf";
            }
            String uniqueName = java.util.UUID.randomUUID().toString() + extension;
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads", "resumes");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            java.nio.file.Path filePath = uploadPath.resolve(uniqueName);
            file.transferTo(filePath);

            String fileUrl = "/uploads/resumes/" + uniqueName;

            return com.yb.partjob.model.dto.ResumeParseResultDTO.builder()
                    .university(university)
                    .skills(skills)
                    .selfIntro(selfIntro)
                    .fileName(originalFilename)
                    .fileUrl(fileUrl)
                    .build();

        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to parse PDF resume", e);
        }
    }

    private String extractUniversity(String text) {
        String[] keywords = { "大学", "学院", "University", "College" };
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            for (String kw : keywords) {
                if (line.contains(kw) && line.length() < 30) {
                    return line.trim();
                }
            }
        }
        return "";
    }

    private java.util.List<String> extractSkills(String text) {
        String lowerText = text.toLowerCase();
        java.util.List<String> knownSkills = java.util.Arrays.asList(
                "java", "spring", "vue", "react", "mysql", "redis", "linux", "c++", "python", "javascript",
                "typescript", "git", "MyBatis");
        java.util.List<String> found = new java.util.ArrayList<>();
        for (String s : knownSkills) {
            if (lowerText.contains(s)) {
                found.add(s);
            }
        }
        return found;
    }

    private String extractSelfIntro(String text) {
        String[] keywords = { "自我评价", "个人总结", "自我介绍", "Personal Summary" };
        String[] lines = text.split("\\r?\\n");
        boolean isCapturing = false;
        StringBuilder intro = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty())
                continue;

            if (!isCapturing) {
                for (String kw : keywords) {
                    if (trimmed.contains(kw)) {
                        isCapturing = true;
                        break;
                    }
                }
            } else {
                // Stop capturing if we hit another possible section like "教育经历", "工作经验"
                if (trimmed.length() < 10
                        && (trimmed.contains("经历") || trimmed.contains("经验") || trimmed.contains("证书"))) {
                    break;
                }
                intro.append(trimmed).append(" ");
                if (intro.length() > 300)
                    break; // Limit length
            }
        }
        return intro.toString().trim();
    }

    @Override
    public Page<InterviewInvitationVO> getMyInvitations(Long studentUserId, int page, int size) {
        Page<InterviewInvitation> invitations = interviewInvitationRepository.findByStudentId(
                studentUserId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return invitations.map(inv -> {
            InterviewInvitationVO vo = new InterviewInvitationVO();
            BeanUtils.copyProperties(inv, vo);

            jobPositionRepository.findById(inv.getJobId())
                    .ifPresent(job -> vo.setJobTitle(job.getTitle()));

            enterpriseInfoRepository.findById(inv.getEnterpriseId())
                    .ifPresent(ent -> vo.setCompanyName(ent.getCompanyName()));

            sysUserRepository.findById(inv.getStudentId())
                    .ifPresent(user -> vo.setStudentName(user.getRealName()));

            return vo;
        });
    }

    @Override
    public void updateInvitationStatus(Long studentUserId, Long invitationId, String status) {
        InterviewInvitation invitation = interviewInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException("Invitation not found"));

        if (!invitation.getStudentId().equals(studentUserId)) {
            throw new BusinessException("Not authorized to update this invitation");
        }

        invitation.setStatus(status);
        interviewInvitationRepository.save(invitation);
    }
}
