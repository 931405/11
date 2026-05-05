package com.yb.partjob.model.dto;

import lombok.Data;

@Data
public class InviteDTO {
    private Long studentId;
    private Long jobId;
    private String greeting;
}
