package com.yb.partjob.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.DataIntegrationRecord;
import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.dto.DataAuthorizationDTO;
import com.yb.partjob.model.dto.DataVerificationRequestDTO;
import com.yb.partjob.repository.DataIntegrationRecordRepository;
import com.yb.partjob.repository.SysConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataIntegrationService {

    private static final String RULE_KEY_COLLECTION = "data_collection_rules";
    private static final String RULE_KEY_CLEANING = "data_cleaning_standard";
    private static final String RULE_KEY_THRESHOLD = "source_quality_threshold";

    @Autowired
    private DataIntegrationRecordRepository recordRepository;

    @Autowired
    private SysConfigRepository sysConfigRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void syncStudentProfile(Long userId, StudentProfile profile) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("university", profile.getUniversity());
        normalized.put("major", profile.getMajor());
        normalized.put("educationLevel", profile.getEducationLevel());
        normalized.put("skills", profile.getSkills());
        normalized.put("expectedLocation", profile.getExpectedLocation());
        normalized.put("availableSchedule", profile.getAvailableSchedule());

        upsertRecord(
                "STUDENT_PROFILE",
                "student_profile",
                "STUDENT",
                profile.getId(),
                userId,
                "AUTHORIZED",
                "VERIFIED",
                computeStudentQuality(profile),
                normalized,
                normalized,
                "v1",
                "Profile data normalized from student-maintained information");
    }

    @Transactional
    public void syncEnterpriseProfile(Long userId, EnterpriseInfo enterprise) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("companyName", enterprise.getCompanyName());
        normalized.put("industry", enterprise.getIndustry());
        normalized.put("companySize", enterprise.getCompanySize());
        normalized.put("contactPerson", enterprise.getContactPerson());
        normalized.put("companyAddress", enterprise.getCompanyAddress());
        normalized.put("certificationStatus", enterprise.getCertificationStatus());

        String verificationStatus = switch (enterprise.getCertificationStatus()) {
            case "APPROVED" -> "VERIFIED";
            case "REJECTED" -> "REJECTED";
            default -> "PENDING";
        };

        upsertRecord(
                "ENTERPRISE_PROFILE",
                "enterprise_profile",
                "ENTERPRISE",
                enterprise.getId(),
                userId,
                "NOT_REQUIRED",
                verificationStatus,
                computeEnterpriseQuality(enterprise),
                normalized,
                normalized,
                "v1",
                enterprise.getCertificationRemark());
    }

    @Transactional
    public void syncJobProfile(Long userId, JobPosition job) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("title", job.getTitle());
        normalized.put("jobType", job.getJobType());
        normalized.put("skillsRequired", job.getSkillsRequired());
        normalized.put("salaryMin", job.getSalaryMin());
        normalized.put("salaryMax", job.getSalaryMax());
        normalized.put("workLocation", job.getWorkLocation());
        normalized.put("workSchedule", job.getWorkSchedule());
        normalized.put("status", job.getStatus());

        upsertRecord(
                "JOB_PROFILE",
                "job_profile",
                "JOB",
                job.getId(),
                userId,
                "NOT_REQUIRED",
                "VERIFIED",
                computeJobQuality(job),
                normalized,
                normalized,
                "v1",
                "Job data normalized from enterprise-maintained posting");
    }

    @Transactional
    public void saveStudentAuthorizations(Long userId, Long studentProfileId, DataAuthorizationDTO dto) {
        upsertAuthorizationRecord(userId, studentProfileId, "ACADEMIC_DATA", dto.getAcademicAuthorized(),
                "academic_profile");
        upsertAuthorizationRecord(userId, studentProfileId, "PREFERENCE_DATA", dto.getPreferenceAuthorized(),
                "student_preference");
        upsertAuthorizationRecord(userId, studentProfileId, "BEHAVIOR_DATA", dto.getBehaviorAuthorized(),
                "behavior_signal");
    }

    @Transactional
    public DataIntegrationRecord submitJobVerificationRequest(Long userId, JobPosition job, DataVerificationRequestDTO dto,
            String sourceName) {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("jobId", job.getId());
        raw.put("title", job.getTitle());
        raw.put("sourceName", dto.getSourceName());
        raw.put("evidence", dto.getEvidence());
        raw.put("remark", dto.getRemark());

        DataIntegrationRecord record = recordRepository
                .findFirstBySourceTypeAndTargetTypeAndTargetId("JOB_VERIFICATION_REQUEST", "JOB", job.getId())
                .orElse(DataIntegrationRecord.builder()
                        .sourceType("JOB_VERIFICATION_REQUEST")
                        .sourceName(sourceName)
                        .targetType("JOB")
                        .targetId(job.getId())
                        .submittedByUserId(userId)
                        .authorizationStatus("NOT_REQUIRED")
                        .build());

        record.setSourceName(dto.getSourceName() != null && !dto.getSourceName().isBlank()
                ? dto.getSourceName()
                : sourceName);
        record.setVerificationStatus("PENDING");
        record.setQualityScore(computeJobQuality(job));
        record.setRawPayload(writeJson(raw));
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("title", job.getTitle());
        normalized.put("location", job.getWorkLocation());
        normalized.put("salaryMin", job.getSalaryMin());
        normalized.put("salaryMax", job.getSalaryMax());
        record.setNormalizedPayload(writeJson(normalized));
        record.setRuleVersion("v1");
        record.setRemark(dto.getRemark());
        record.setLastVerifiedAt(null);
        return recordRepository.save(record);
    }

    public List<DataIntegrationRecord> getTargetRecords(String targetType, Long targetId) {
        return recordRepository.findByTargetTypeAndTargetIdOrderByUpdatedAtDesc(targetType, targetId);
    }

    public Page<DataIntegrationRecord> getAdminRecords(String targetType, String verificationStatus, String sourceType,
            int page, int size) {
        return recordRepository.search(targetType, verificationStatus, sourceType,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updatedAt")));
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalRecords", recordRepository.count());
        overview.put("pendingRecords", recordRepository.countByVerificationStatus("PENDING"));
        overview.put("verifiedRecords", recordRepository.countByVerificationStatus("VERIFIED"));
        overview.put("rejectedRecords", recordRepository.countByVerificationStatus("REJECTED"));
        overview.put("studentRecords", recordRepository.countByTargetType("STUDENT"));
        overview.put("enterpriseRecords", recordRepository.countByTargetType("ENTERPRISE"));
        overview.put("jobRecords", recordRepository.countByTargetType("JOB"));
        overview.put("averageQualityScore", recordRepository.averageQualityScore().setScale(2, RoundingMode.HALF_UP));
        return overview;
    }

    public Map<String, String> getRules() {
        Map<String, String> rules = new LinkedHashMap<>();
        rules.put("data_collection_rules", getRuleValue(RULE_KEY_COLLECTION,
                "Collect profile, behavior, enterprise and posting data after user authorization or platform approval."));
        rules.put("data_cleaning_standard", getRuleValue(RULE_KEY_CLEANING,
                "Trim blanks, normalize skills/location labels, reject empty key fields, keep latest verified snapshot."));
        rules.put("source_quality_threshold", getRuleValue(RULE_KEY_THRESHOLD, "75"));
        return rules;
    }

    @Transactional
    public void saveRules(Map<String, String> rules) {
        saveRule(RULE_KEY_COLLECTION, rules.get(RULE_KEY_COLLECTION), "Collection rule for integration sources");
        saveRule(RULE_KEY_CLEANING, rules.get(RULE_KEY_CLEANING), "Cleaning rule for normalized records");
        saveRule(RULE_KEY_THRESHOLD, rules.get(RULE_KEY_THRESHOLD), "Quality threshold for highlighted records");
    }

    @Transactional
    public void reviewRecord(Long recordId, String status, String remark) {
        if (!"VERIFIED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("Unsupported review status");
        }
        DataIntegrationRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("Integration record not found"));
        record.setVerificationStatus(status);
        record.setRemark(remark);
        record.setLastVerifiedAt(LocalDateTime.now());
        recordRepository.save(record);
    }

    private void upsertAuthorizationRecord(Long userId, Long studentProfileId, String sourceType, Boolean authorized,
            String sourceName) {
        DataIntegrationRecord record = recordRepository
                .findFirstBySourceTypeAndTargetTypeAndTargetId(sourceType, "STUDENT", studentProfileId)
                .orElse(DataIntegrationRecord.builder()
                        .sourceType(sourceType)
                        .sourceName(sourceName)
                        .targetType("STUDENT")
                        .targetId(studentProfileId)
                        .submittedByUserId(userId)
                        .build());

        boolean enabled = Boolean.TRUE.equals(authorized);
        record.setAuthorizationStatus(enabled ? "AUTHORIZED" : "REVOKED");
        record.setVerificationStatus(enabled ? "VERIFIED" : "PENDING");
        record.setQualityScore(enabled ? new BigDecimal("100.00") : new BigDecimal("0.00"));
        record.setRawPayload(writeJson(Map.of("authorized", enabled, "updatedBy", userId)));
        record.setNormalizedPayload(writeJson(Map.of("source", sourceName, "authorized", enabled)));
        record.setRuleVersion("v1");
        record.setRemark(enabled ? "Student granted access to this data source" : "Student revoked access");
        recordRepository.save(record);
    }

    private void upsertRecord(String sourceType, String sourceName, String targetType, Long targetId, Long userId,
            String authorizationStatus, String verificationStatus, BigDecimal qualityScore, Object rawPayload,
            Object normalizedPayload, String ruleVersion, String remark) {
        DataIntegrationRecord record = recordRepository
                .findFirstBySourceTypeAndTargetTypeAndTargetId(sourceType, targetType, targetId)
                .orElse(DataIntegrationRecord.builder()
                        .sourceType(sourceType)
                        .sourceName(sourceName)
                        .targetType(targetType)
                        .targetId(targetId)
                        .submittedByUserId(userId)
                        .build());

        record.setSourceName(sourceName);
        record.setSubmittedByUserId(userId);
        record.setAuthorizationStatus(authorizationStatus);
        record.setVerificationStatus(verificationStatus);
        record.setQualityScore(qualityScore);
        record.setRawPayload(writeJson(rawPayload));
        record.setNormalizedPayload(writeJson(normalizedPayload));
        record.setRuleVersion(ruleVersion);
        record.setRemark(remark);
        recordRepository.save(record);
    }

    private BigDecimal computeStudentQuality(StudentProfile profile) {
        int total = 7;
        int score = 0;
        if (filled(profile.getUniversity())) score++;
        if (filled(profile.getMajor())) score++;
        if (filled(profile.getEducationLevel())) score++;
        if (filled(profile.getSkills())) score++;
        if (filled(profile.getExpectedLocation())) score++;
        if (filled(profile.getAvailableSchedule())) score++;
        if (filled(profile.getSelfIntro())) score++;
        return toPercent(score, total);
    }

    private BigDecimal computeEnterpriseQuality(EnterpriseInfo enterprise) {
        int total = 6;
        int score = 0;
        if (filled(enterprise.getCompanyName())) score++;
        if (filled(enterprise.getIndustry())) score++;
        if (filled(enterprise.getCompanySize())) score++;
        if (filled(enterprise.getContactPerson())) score++;
        if (filled(enterprise.getCompanyAddress())) score++;
        if (filled(enterprise.getDescription())) score++;
        return toPercent(score, total);
    }

    private BigDecimal computeJobQuality(JobPosition job) {
        int total = 7;
        int score = 0;
        if (filled(job.getTitle())) score++;
        if (filled(job.getDescription())) score++;
        if (filled(job.getRequirements())) score++;
        if (filled(job.getSkillsRequired())) score++;
        if (job.getSalaryMin() != null || job.getSalaryMax() != null) score++;
        if (filled(job.getWorkLocation())) score++;
        if (filled(job.getWorkSchedule())) score++;
        return toPercent(score, total);
    }

    private BigDecimal toPercent(int value, int total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private boolean filled(String value) {
        return value != null && !value.isBlank();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String getRuleValue(String key, String fallback) {
        return sysConfigRepository.findByConfigKey(key)
                .map(config -> config.getConfigValue())
                .orElse(fallback);
    }

    private void saveRule(String key, String value, String description) {
        if (value == null) {
            return;
        }
        var config = sysConfigRepository.findByConfigKey(key)
                .orElse(com.yb.partjob.model.SysConfig.builder()
                        .configKey(key)
                        .description(description)
                        .build());
        config.setConfigValue(value);
        config.setDescription(description);
        sysConfigRepository.save(config);
    }
}
