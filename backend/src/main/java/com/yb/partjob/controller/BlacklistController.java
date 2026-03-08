package com.yb.partjob.controller;

import com.yb.partjob.model.vo.BlacklistVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/blacklist")
public class BlacklistController {

    @Autowired
    private IBlacklistService blacklistService;

    @GetMapping
    public Result<List<BlacklistVO>> getBlacklist(Authentication auth) {
        Long studentId = (Long) auth.getPrincipal();
        return Result.success(blacklistService.getBlacklist(studentId));
    }

    @PostMapping("/{enterpriseId}")
    public Result<Void> addBlacklist(Authentication auth, @PathVariable Long enterpriseId) {
        Long studentId = (Long) auth.getPrincipal();
        blacklistService.addBlacklist(studentId, enterpriseId);
        return Result.success();
    }

    @DeleteMapping("/{enterpriseId}")
    public Result<Void> removeBlacklist(Authentication auth, @PathVariable Long enterpriseId) {
        Long studentId = (Long) auth.getPrincipal();
        blacklistService.removeBlacklist(studentId, enterpriseId);
        return Result.success();
    }
}
