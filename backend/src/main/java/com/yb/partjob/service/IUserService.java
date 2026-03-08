package com.yb.partjob.service;

import com.yb.partjob.model.dto.UpdateUserDTO;

public interface IUserService {
    void updateUserInfo(Long userId, UpdateUserDTO dto);

    void deleteAccount(Long userId);
}
