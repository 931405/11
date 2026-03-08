package com.yb.partjob.service.impl;

import com.yb.partjob.config.JwtUtil;
import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.SysUser;
import com.yb.partjob.model.dto.LoginDTO;
import com.yb.partjob.model.dto.RegisterDTO;
import com.yb.partjob.model.vo.LoginVO;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid username or password"));

        if (!loginDTO.getPassword().equals(user.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("Account has been disabled");
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
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("Username already exists");
        }

        SysUser user = SysUser.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .realName(dto.getRealName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .role(dto.getRole())
                .status(1)
                .build();
        userRepository.save(user);

        if ("STUDENT".equals(dto.getRole())) {
            StudentProfile profile = StudentProfile.builder()
                    .userId(user.getId())
                    .university(dto.getUniversity())
                    .major(dto.getMajor())
                    .educationLevel(dto.getEducationLevel())
                    .enrollmentYear(dto.getEnrollmentYear())
                    .build();
            studentProfileRepository.save(profile);
        } else if ("ENTERPRISE".equals(dto.getRole())) {
            EnterpriseInfo info = EnterpriseInfo.builder()
                    .userId(user.getId())
                    .companyName(dto.getCompanyName() != null ? dto.getCompanyName() : "Unnamed Company")
                    .industry(dto.getIndustry())
                    .companySize(dto.getCompanySize())
                    .contactPerson(dto.getContactPerson())
                    .contactPhone(dto.getContactPhone())
                    .companyAddress(dto.getCompanyAddress())
                    .description(dto.getDescription())
                    .certificationStatus("PENDING")
                    .build();
            enterpriseInfoRepository.save(info);
        }

        return user.getId();
    }

    @Override
    public void changePassword(Long userId, com.yb.partjob.model.dto.ChangePasswordDTO dto) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!dto.getOldPassword().equals(user.getPassword())) {
            throw new BusinessException("当前密码不正确");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }

        user.setPassword(dto.getNewPassword());
        userRepository.save(user);
    }
}
