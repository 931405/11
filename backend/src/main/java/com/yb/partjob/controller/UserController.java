package com.yb.partjob.controller;

import com.yb.partjob.model.SysUser;
import com.yb.partjob.model.dto.UpdateUserDTO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.repository.SysUserRepository;
import com.yb.partjob.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private SysUserRepository userRepository;

    @PutMapping("/info")
    public Result<Void> updateUserInfo(Authentication auth, @RequestBody UpdateUserDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        userService.updateUserInfo(userId, dto);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<SysUser> getUserInfo(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userRepository.findById(userId).orElse(null));
    }

    @DeleteMapping("/account")
    public Result<Void> deleteAccount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userService.deleteAccount(userId);
        return Result.success();
    }
}
