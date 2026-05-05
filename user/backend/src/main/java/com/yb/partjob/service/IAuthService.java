package com.yb.partjob.service;

import com.yb.partjob.model.dto.LoginDTO;
import com.yb.partjob.model.dto.RegisterDTO;
import com.yb.partjob.model.vo.LoginVO;

public interface IAuthService {
    LoginVO login(LoginDTO loginDTO);

    Long register(RegisterDTO registerDTO);

    void changePassword(Long userId, com.yb.partjob.model.dto.ChangePasswordDTO dto);
}
