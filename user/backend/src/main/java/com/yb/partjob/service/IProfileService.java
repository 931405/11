package com.yb.partjob.service;

import com.yb.partjob.model.dto.UpdateProfileDTO;
import com.yb.partjob.model.vo.ProfileVO;

public interface IProfileService {
    ProfileVO getCurrentProfile(Long userId);

    ProfileVO updateCurrentProfile(Long userId, UpdateProfileDTO dto);
}
