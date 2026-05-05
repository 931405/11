package com.yb.partjob.service.impl;

import com.yb.partjob.exception.BusinessException;
import com.yb.partjob.model.SysUser;
import com.yb.partjob.model.dto.UpdateUserDTO;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private SysUserRepository userRepository;

    @Override
    public void updateUserInfo(Long userId, UpdateUserDTO dto) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()
                && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new BusinessException("Username already exists");
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        userRepository.save(user);
    }

    @Override
    public void deleteAccount(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setStatus(0); // 0 means disabled/deleted
        userRepository.save(user);
    }
}
