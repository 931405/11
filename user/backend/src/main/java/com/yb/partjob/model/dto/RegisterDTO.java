package com.yb.partjob.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "请输入用户名")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 50, message = "密码长度需为6到50个字符")
    private String password;

    @NotBlank(message = "请选择用户角色")
    private String role;

    private String realName;
    private String phone;
    private String email;

    // Enterprise fields
    private String companyName;
    private String industry;
    private String companySize;
    private String contactPerson;
    private String contactPhone;
    private String companyAddress;
    private String description;

    // Student fields
    private String university;
    private String major;
    private String educationLevel;
    private Integer enrollmentYear;
}
