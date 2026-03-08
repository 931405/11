package com.yb.partjob.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Role is required")
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
