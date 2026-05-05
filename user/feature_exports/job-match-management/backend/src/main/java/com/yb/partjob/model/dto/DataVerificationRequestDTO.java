package com.yb.partjob.model.dto;

import lombok.Data;

@Data
public class DataVerificationRequestDTO {
    private String sourceName;
    private String evidence;
    private String remark;
}
