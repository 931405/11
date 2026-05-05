package com.yb.partjob.model.vo;

import lombok.Data;

@Data
public class ProfileVO {
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String role;

    private String university;
    private String major;
    private String educationLevel;
    private Integer enrollmentYear;
    private String selfIntro;
    private String expectedLocation;
    private String availableSchedule;

    private String companyName;
    private String industry;
    private String companySize;
    private String contactPerson;
    private String contactPhone;
    private String companyAddress;
    private String description;
    private String certificationStatus;
}
