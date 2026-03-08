package com.yb.partjob.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yb.partjob.model.JobPosition;
import com.yb.partjob.model.MatchScore;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.repository.JobPositionRepository;
import com.yb.partjob.repository.MatchScoreRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.service.IMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatchServiceImpl implements IMatchService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private MatchScoreRepository matchScoreRepository;

    @Autowired
    private com.yb.partjob.repository.SysConfigRepository sysConfigRepository;

    @Autowired
    private com.yb.partjob.repository.UserBehaviorRepository userBehaviorRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private BigDecimal getWeight(String key, String defaultVal) {
        return sysConfigRepository.findByConfigKey(key)
                .map(config -> new BigDecimal(config.getConfigValue()))
                .orElse(new BigDecimal(defaultVal));
    }

    @Override
    @Transactional
    public void computeMatchScores(Long studentId) {
        StudentProfile student = studentProfileRepository.findById(studentId).orElse(null);
        if (student == null)
            return;

        // Get all open jobs
        List<JobPosition> openJobs = jobPositionRepository.findByStatus("OPEN",
                PageRequest.of(0, 500)).getContent();

        java.util.Set<Long> processedJobIds = new java.util.HashSet<>();

        // Fetch recent behaviors to adjust weights dynamically
        List<com.yb.partjob.model.UserBehavior> behaviors = userBehaviorRepository
                .findTop50ByStudentIdOrderByCreatedAtDesc(studentId);

        // Base weights from global config
        BigDecimal baseSkillW = getWeight("skill_weight", "0.40");
        BigDecimal baseSalaryW = getWeight("salary_weight", "0.25");
        BigDecimal baseLocationW = getWeight("location_weight", "0.20");
        BigDecimal baseScheduleW = getWeight("schedule_weight", "0.15");

        BigDecimal dynSkillW = baseSkillW;
        BigDecimal dynSalaryW = baseSalaryW;
        BigDecimal dynLocationW = baseLocationW;
        BigDecimal dynScheduleW = baseScheduleW;

        if (!behaviors.isEmpty()) {
            BigDecimal sumSkill = BigDecimal.ZERO;
            BigDecimal sumSalary = BigDecimal.ZERO;
            BigDecimal sumLocation = BigDecimal.ZERO;
            BigDecimal sumSchedule = BigDecimal.ZERO;
            int validCount = 0;

            for (com.yb.partjob.model.UserBehavior b : behaviors) {
                JobPosition viewedJob = jobPositionRepository.findById(b.getJobId()).orElse(null);
                if (viewedJob != null) {
                    sumSkill = sumSkill.add(computeSkillScore(student, viewedJob));
                    sumSalary = sumSalary.add(computeSalaryScore(student, viewedJob));
                    sumLocation = sumLocation.add(computeLocationScore(student, viewedJob));
                    sumSchedule = sumSchedule.add(computeScheduleScore(student, viewedJob));
                    validCount++;
                }
            }

            if (validCount > 0) {
                BigDecimal divisor = new BigDecimal(validCount).multiply(HUNDRED);
                // The more consistently high a dimension's score is in the viewed jobs, the
                // higher its ratio
                BigDecimal skillRatio = sumSkill.divide(divisor, 4, RoundingMode.HALF_UP).add(new BigDecimal("0.5")); // baseline
                                                                                                                      // 0.5
                BigDecimal salaryRatio = sumSalary.divide(divisor, 4, RoundingMode.HALF_UP).add(new BigDecimal("0.5"));
                BigDecimal locationRatio = sumLocation.divide(divisor, 4, RoundingMode.HALF_UP)
                        .add(new BigDecimal("0.5"));
                BigDecimal scheduleRatio = sumSchedule.divide(divisor, 4, RoundingMode.HALF_UP)
                        .add(new BigDecimal("0.5"));

                // Combine base weights with dynamic ratios
                BigDecimal rawSkill = baseSkillW.multiply(skillRatio);
                BigDecimal rawSalary = baseSalaryW.multiply(salaryRatio);
                BigDecimal rawLocation = baseLocationW.multiply(locationRatio);
                BigDecimal rawSchedule = baseScheduleW.multiply(scheduleRatio);

                BigDecimal totalRaw = rawSkill.add(rawSalary).add(rawLocation).add(rawSchedule);
                if (totalRaw.compareTo(BigDecimal.ZERO) > 0) {
                    dynSkillW = rawSkill.divide(totalRaw, 4, RoundingMode.HALF_UP);
                    dynSalaryW = rawSalary.divide(totalRaw, 4, RoundingMode.HALF_UP);
                    dynLocationW = rawLocation.divide(totalRaw, 4, RoundingMode.HALF_UP);
                    dynScheduleW = rawSchedule.divide(totalRaw, 4, RoundingMode.HALF_UP);
                }
            }
        }

        for (JobPosition job : openJobs) {
            if (!processedJobIds.add(job.getId()))
                continue;

            BigDecimal skillScore = computeSkillScore(student, job);
            BigDecimal salaryScore = computeSalaryScore(student, job);
            BigDecimal locationScore = computeLocationScore(student, job);
            BigDecimal scheduleScore = computeScheduleScore(student, job);

            BigDecimal totalScore = skillScore.multiply(dynSkillW)
                    .add(salaryScore.multiply(dynSalaryW))
                    .add(locationScore.multiply(dynLocationW))
                    .add(scheduleScore.multiply(dynScheduleW))
                    .setScale(2, RoundingMode.HALF_UP);

            MatchScore score = matchScoreRepository.findByStudentIdAndJobId(studentId, job.getId())
                    .orElse(MatchScore.builder()
                            .studentId(studentId)
                            .jobId(job.getId())
                            .build());

            score.setTotalScore(totalScore);
            score.setSkillScore(skillScore);
            score.setSalaryScore(salaryScore);
            score.setLocationScore(locationScore);
            score.setScheduleScore(scheduleScore);

            matchScoreRepository.save(score);
        }
    }

    @Override
    public Page<MatchScore> getTopMatches(Long studentId, int page, int size) {
        return matchScoreRepository.findByStudentIdOrderByTotalScoreDesc(studentId,
                PageRequest.of(page - 1, size));
    }

    private BigDecimal computeSkillScore(StudentProfile student, JobPosition job) {
        Set<String> studentSkills = parseSkills(student.getSkills());
        Set<String> jobSkills = parseSkills(job.getSkillsRequired());

        if (jobSkills.isEmpty())
            return HUNDRED;
        if (studentSkills.isEmpty())
            return BigDecimal.ZERO;

        long matchCount = jobSkills.stream()
                .filter(js -> studentSkills.stream().anyMatch(ss -> ss.equalsIgnoreCase(js)))
                .count();

        return new BigDecimal(matchCount)
                .divide(new BigDecimal(jobSkills.size()), 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSalaryScore(StudentProfile student, JobPosition job) {
        if (student.getExpectedSalaryMin() == null || job.getSalaryMax() == null) {
            return new BigDecimal("50");
        }

        BigDecimal studentMin = student.getExpectedSalaryMin();
        BigDecimal studentMax = student.getExpectedSalaryMax() != null ? student.getExpectedSalaryMax() : studentMin;
        BigDecimal jobMin = job.getSalaryMin() != null ? job.getSalaryMin() : BigDecimal.ZERO;
        BigDecimal jobMax = job.getSalaryMax();

        // Perfect overlap
        if (jobMax.compareTo(studentMin) >= 0 && jobMin.compareTo(studentMax) <= 0) {
            return HUNDRED;
        }

        // Partial overlap
        BigDecimal gap;
        if (jobMax.compareTo(studentMin) < 0) {
            gap = studentMin.subtract(jobMax);
        } else {
            gap = jobMin.subtract(studentMax);
        }

        BigDecimal range = studentMax.subtract(studentMin).max(BigDecimal.ONE);
        BigDecimal ratio = BigDecimal.ONE.subtract(gap.divide(range, 4, RoundingMode.HALF_UP));

        return ratio.max(BigDecimal.ZERO).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeLocationScore(StudentProfile student, JobPosition job) {
        if (student.getExpectedLocation() == null || job.getWorkLocation() == null) {
            return new BigDecimal("50");
        }

        String studentLocation = student.getExpectedLocation().toLowerCase();
        String jobLocation = job.getWorkLocation().toLowerCase();

        if (studentLocation.contains(jobLocation) || jobLocation.contains(studentLocation)) {
            return HUNDRED;
        }

        return new BigDecimal("20");
    }

    private BigDecimal computeScheduleScore(StudentProfile student, JobPosition job) {
        if (student.getAvailableSchedule() == null || job.getWorkSchedule() == null) {
            return new BigDecimal("50");
        }

        String studentSchedule = student.getAvailableSchedule().toLowerCase();
        String jobSchedule = job.getWorkSchedule().toLowerCase();

        if (studentSchedule.contains(jobSchedule) || jobSchedule.contains(studentSchedule)) {
            return HUNDRED;
        }

        return new BigDecimal("30");
    }

    private Set<String> parseSkills(String skillsJson) {
        if (skillsJson == null || skillsJson.isBlank()) {
            return Collections.emptySet();
        }
        try {
            List<String> list = objectMapper.readValue(skillsJson, new TypeReference<List<String>>() {
            });
            return new HashSet<>(list);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}
