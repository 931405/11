package com.yb.partjob.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserBehaviorDTO {
    @NotNull(message = "Job ID cannot be null")
    private Long jobId;

    @NotNull(message = "Action type cannot be null")
    private String actionType;

    private Integer dwellTime;
}
