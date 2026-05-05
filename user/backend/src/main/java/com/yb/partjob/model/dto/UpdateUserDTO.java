package com.yb.partjob.model.dto;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private String username;
    private String realName;
    private String email;
    private String phone;
}
