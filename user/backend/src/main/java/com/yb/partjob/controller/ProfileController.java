package com.yb.partjob.controller;

import com.yb.partjob.model.dto.UpdateProfileDTO;
import com.yb.partjob.model.vo.ProfileVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private IProfileService profileService;

    @GetMapping("/me")
    public Result<ProfileVO> getMyProfile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(profileService.getCurrentProfile(userId));
    }

    @PutMapping("/me")
    public Result<ProfileVO> updateMyProfile(Authentication auth, @Valid @RequestBody UpdateProfileDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(profileService.updateCurrentProfile(userId, dto));
    }
}
