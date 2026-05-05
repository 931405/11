package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.StudentProfile;
import com.yb.partjob.model.SysUser;
import com.yb.partjob.model.dto.UpdateProfileDTO;
import com.yb.partjob.model.vo.ProfileVO;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.StudentProfileRepository;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProfileServiceImpl implements IProfileService {

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileVO getCurrentProfile(Long userId) {
        SysUser user = getUser(userId);
        return buildProfile(user);
    }

    @Override
    @Transactional
    public ProfileVO updateCurrentProfile(Long userId, UpdateProfileDTO dto) {
        SysUser user = getUser(userId);
        updateBaseUser(user, dto);
        userRepository.save(user);

        if ("STUDENT".equals(user.getRole())) {
            updateStudentProfile(userId, dto);
        } else if ("ENTERPRISE".equals(user.getRole())) {
            updateEnterpriseProfile(userId, dto);
        }

        return buildProfile(userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在")));
    }

    private SysUser getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private ProfileVO buildProfile(SysUser user) {
        ProfileVO vo = new ProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());

        if ("STUDENT".equals(user.getRole())) {
            studentProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                vo.setUniversity(profile.getUniversity());
                vo.setMajor(profile.getMajor());
                vo.setEducationLevel(profile.getEducationLevel());
                vo.setEnrollmentYear(profile.getEnrollmentYear());
                vo.setSelfIntro(profile.getSelfIntro());
                vo.setExpectedLocation(profile.getExpectedLocation());
                vo.setAvailableSchedule(profile.getAvailableSchedule());
            });
        } else if ("ENTERPRISE".equals(user.getRole())) {
            enterpriseInfoRepository.findByUserId(user.getId()).ifPresent(info -> {
                vo.setCompanyName(info.getCompanyName());
                vo.setIndustry(info.getIndustry());
                vo.setCompanySize(info.getCompanySize());
                vo.setContactPerson(info.getContactPerson());
                vo.setContactPhone(info.getContactPhone());
                vo.setCompanyAddress(info.getCompanyAddress());
                vo.setDescription(info.getDescription());
                vo.setCertificationStatus(info.getCertificationStatus());
            });
        }

        return vo;
    }

    private void updateBaseUser(SysUser user, UpdateProfileDTO dto) {
        if (dto.getUsername() != null) {
            if (!StringUtils.hasText(dto.getUsername())) {
                throw new BusinessException("用户名不能为空");
            }

            String username = dto.getUsername().trim();
            if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
                throw new BusinessException("用户名已存在");
            }

            user.setUsername(username);
        }

        if (dto.getRealName() != null) {
            user.setRealName(normalize(dto.getRealName()));
        }
        if (dto.getPhone() != null) {
            user.setPhone(normalize(dto.getPhone()));
        }
        if (dto.getEmail() != null) {
            user.setEmail(normalize(dto.getEmail()));
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(normalize(dto.getAvatar()));
        }
    }

    private void updateStudentProfile(Long userId, UpdateProfileDTO dto) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> StudentProfile.builder().userId(userId).build());

        if (dto.getUniversity() != null) {
            profile.setUniversity(normalize(dto.getUniversity()));
        }
        if (dto.getMajor() != null) {
            profile.setMajor(normalize(dto.getMajor()));
        }
        if (dto.getEducationLevel() != null) {
            profile.setEducationLevel(normalize(dto.getEducationLevel()));
        }
        if (dto.getEnrollmentYear() != null) {
            profile.setEnrollmentYear(dto.getEnrollmentYear());
        }
        if (dto.getSelfIntro() != null) {
            profile.setSelfIntro(normalize(dto.getSelfIntro()));
        }
        if (dto.getExpectedLocation() != null) {
            profile.setExpectedLocation(normalize(dto.getExpectedLocation()));
        }
        if (dto.getAvailableSchedule() != null) {
            profile.setAvailableSchedule(normalize(dto.getAvailableSchedule()));
        }

        studentProfileRepository.save(profile);
    }

    private void updateEnterpriseProfile(Long userId, UpdateProfileDTO dto) {
        EnterpriseInfo info = enterpriseInfoRepository.findByUserId(userId)
                .orElseGet(() -> EnterpriseInfo.builder()
                        .userId(userId)
                        .companyName("未命名企业")
                        .certificationStatus("PENDING")
                        .build());

        if (dto.getCompanyName() != null) {
            if (!StringUtils.hasText(dto.getCompanyName())) {
                throw new BusinessException("企业名称不能为空");
            }
            info.setCompanyName(dto.getCompanyName().trim());
        }
        if (dto.getIndustry() != null) {
            info.setIndustry(normalize(dto.getIndustry()));
        }
        if (dto.getCompanySize() != null) {
            info.setCompanySize(normalize(dto.getCompanySize()));
        }
        if (dto.getContactPerson() != null) {
            info.setContactPerson(normalize(dto.getContactPerson()));
        }
        if (dto.getContactPhone() != null) {
            info.setContactPhone(normalize(dto.getContactPhone()));
        }
        if (dto.getCompanyAddress() != null) {
            info.setCompanyAddress(normalize(dto.getCompanyAddress()));
        }
        if (dto.getDescription() != null) {
            info.setDescription(normalize(dto.getDescription()));
        }

        enterpriseInfoRepository.save(info);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
