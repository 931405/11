package com.yb.partjob.service;

import com.yb.partjob.model.SysUser;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface IAdminService {
    Map<String, Object> getDashboardStats();

    Page<SysUser> getUserList(String role, int page, int size);

    void updateUserStatus(Long userId, Integer status);

    Map<String, String> getMatchSettings();

    void saveMatchSettings(Map<String, String> settings);
}
