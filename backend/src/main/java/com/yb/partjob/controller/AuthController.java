package com.yb.partjob.controller;

import com.yb.partjob.model.dto.LoginDTO;
import com.yb.partjob.model.dto.RegisterDTO;
import com.yb.partjob.model.vo.LoginVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IAuthService;
import jakarta.validation.Valid;
import com.yb.partjob.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @LogOperation("User Login")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO vo = authService.login(loginDTO);
        return Result.success(vo);
    }

    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterDTO registerDTO) {
        Long userId = authService.register(registerDTO);
        return Result.success(userId);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(org.springframework.security.core.Authentication auth,
            @RequestBody com.yb.partjob.model.dto.ChangePasswordDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        authService.changePassword(userId, dto);
        return Result.success();
    }
}
