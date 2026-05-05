package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.SysConfig;
import com.yb.partjob.model.SysUser;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.JobApplicationRepository;
import com.yb.partjob.repository.JobPositionRepository;
import com.yb.partjob.repository.SysConfigRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements IAdminService {

    private static final Map<String, String> DEFAULT_SETTINGS = Map.of(
            "skill_weight", "0.40",
            "salary_weight", "0.25",
            "location_weight", "0.20",
            "schedule_weight", "0.15");

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalStudents", userRepository.countByRole("STUDENT"));
        stats.put("totalEnterprises", userRepository.countByRole("ENTERPRISE"));
        stats.put("totalAdmins", userRepository.countByRole("ADMIN"));
        stats.put("totalJobs", jobPositionRepository.countByStatus("OPEN"));
        stats.put("pendingAudits", enterpriseInfoRepository.countByCertificationStatus("PENDING"));
        stats.put("totalApplications", applicationRepository.count());

        List<String> trendDays = new ArrayList<>();
        List<Long> trendStudentData = new ArrayList<>();
        List<Long> trendAppData = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            trendDays.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            trendStudentData.add(userRepository.countByRoleAndCreatedAtBetween("STUDENT", start, end));
            trendAppData.add(applicationRepository.countByCreatedAtBetween(start, end));
        }

        stats.put("trendDays", trendDays);
        stats.put("trendStudentData", trendStudentData);
        stats.put("trendAppData", trendAppData);
        return stats;
    }

    @Override
    public Page<SysUser> getUserList(String role, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 1) - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (role != null && !role.isBlank()) {
            return userRepository.findByRole(role, pageRequest);
        }
        return userRepository.findAll(pageRequest);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if ("ADMIN".equals(user.getRole()) && Integer.valueOf(0).equals(status)) {
            throw new BusinessException("管理员账号不能被禁用");
        }
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public Map<String, String> getMatchSettings() {
        Map<String, String> settings = new LinkedHashMap<>(DEFAULT_SETTINGS);
        sysConfigRepository.findAll().forEach(config -> settings.put(config.getConfigKey(), config.getConfigValue()));
        return settings;
    }

    @Override
    @Transactional
    public void saveMatchSettings(Map<String, String> settings) {
        Map<String, String> merged = new LinkedHashMap<>(DEFAULT_SETTINGS);
        if (settings != null) {
            merged.putAll(settings);
        }

        merged.forEach((key, value) -> {
            SysConfig config = sysConfigRepository.findByConfigKey(key)
                    .orElse(SysConfig.builder().configKey(key).description("匹配算法参数").build());
            config.setConfigValue(value);
            sysConfigRepository.save(config);
        });
    }
}
