package com.yb.partjob.controller;

import com.yb.partjob.model.SysUser;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public Result<Page<SysUser>> getUserList(@RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminService.getUserList(role, page, size));
    }

    @PutMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        adminService.updateUserStatus(userId, status);
        return Result.success();
    }

    @GetMapping("/settings/match")
    public Result<Map<String, String>> getMatchSettings() {
        return Result.success(adminService.getMatchSettings());
    }

    @PutMapping("/settings/match")
    public Result<Void> saveMatchSettings(@RequestBody Map<String, String> settings) {
        adminService.saveMatchSettings(settings);
        return Result.success();
    }
}
