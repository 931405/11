package com.yb.partjob.service.impl;

import com.yb.partjob.config.JwtUtil;
import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.SysUser;
import com.yb.partjob.model.dto.ChangePasswordDTO;
import com.yb.partjob.model.dto.LoginDTO;
import com.yb.partjob.model.dto.RegisterDTO;
import com.yb.partjob.model.vo.LoginVO;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordMatches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被停用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setToken(token);
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    @Override
    @Transactional
    public Long register(RegisterDTO dto) {
        String username = dto.getUsername() == null ? "" : dto.getUsername().trim();
        if (username.isEmpty()) {
            throw new BusinessException("请输入用户名");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (!"STUDENT".equals(dto.getRole()) && !"ENTERPRISE".equals(dto.getRole()) && !"ADMIN".equals(dto.getRole())) {
            throw new BusinessException("用户角色不合法");
        }

        SysUser user = SysUser.builder()
                .username(username)
                .password(dto.getPassword())
                .realName(normalize(dto.getRealName()))
                .phone(normalize(dto.getPhone()))
                .email(normalize(dto.getEmail()))
                .role(dto.getRole())
                .status(1)
                .build();
        userRepository.save(user);

        if ("STUDENT".equals(dto.getRole())) {
            StudentProfile profile = StudentProfile.builder()
                    .userId(user.getId())
                    .university(normalize(dto.getUniversity()))
                    .major(normalize(dto.getMajor()))
                    .educationLevel(normalize(dto.getEducationLevel()))
                    .enrollmentYear(dto.getEnrollmentYear())
                    .skills("[]")
                    .resumeAttachments("[]")
                    .privacyResumeOpen(true)
                    .privacyShowName(true)
                    .privacyShowContact(false)
                    .privacyShowOnline(true)
                    .privacyShowLastActive(true)
                    .notifyNewJob(true)
                    .notifyApplicationStatus(true)
                    .notifyInterview(true)
                    .notifyNewMessage(true)
                    .notifyMessageSound(false)
                    .notifySystemAnnouncement(true)
                    .notifyPromotion(false)
                    .build();
            studentProfileRepository.save(profile);
        } else if ("ENTERPRISE".equals(dto.getRole())) {
            EnterpriseInfo info = EnterpriseInfo.builder()
                    .userId(user.getId())
                    .companyName(StringUtils.hasText(dto.getCompanyName()) ? dto.getCompanyName().trim() : "未命名企业")
                    .industry(normalize(dto.getIndustry()))
                    .companySize(normalize(dto.getCompanySize()))
                    .contactPerson(normalize(dto.getContactPerson()))
                    .contactPhone(normalize(dto.getContactPhone()))
                    .companyAddress(normalize(dto.getCompanyAddress()))
                    .description(normalize(dto.getDescription()))
                    .certificationStatus("PENDING")
                    .build();
            enterpriseInfoRepository.save(info);
        }

        return user.getId();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordMatches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("当前密码不正确");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于 6 位");
        }

        user.setPassword(dto.getNewPassword());
        userRepository.save(user);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (rawPassword.equals(storedPassword)) {
            return true;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return bCryptPasswordEncoder.matches(rawPassword, storedPassword);
        }
        return false;
    }
}
