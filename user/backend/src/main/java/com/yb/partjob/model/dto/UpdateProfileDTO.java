package com.yb.partjob.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String realName;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 500, message = "头像地址长度不能超过500个字符")
    private String avatar;

    @Size(max = 100, message = "学校名称长度不能超过100个字符")
    private String university;

    @Size(max = 100, message = "专业名称长度不能超过100个字符")
    private String major;

    @Size(max = 20, message = "学历长度不能超过20个字符")
    private String educationLevel;

    private Integer enrollmentYear;

    private String selfIntro;

    @Size(max = 200, message = "期望地点长度不能超过200个字符")
    private String expectedLocation;

    @Size(max = 500, message = "可工作时间长度不能超过500个字符")
    private String availableSchedule;

    @Size(max = 200, message = "企业名称长度不能超过200个字符")
    private String companyName;

    @Size(max = 100, message = "行业长度不能超过100个字符")
    private String industry;

    @Size(max = 50, message = "企业规模长度不能超过50个字符")
    private String companySize;

    @Size(max = 50, message = "联系人长度不能超过50个字符")
    private String contactPerson;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    @Size(max = 500, message = "公司地址长度不能超过500个字符")
    private String companyAddress;

    private String description;
}
