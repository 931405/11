package com.yb.partjob.model.dto;

import lombok.Data;

@Data
public class DataAuthorizationDTO {
    private Boolean academicAuthorized;
    private Boolean preferenceAuthorized;
    private Boolean behaviorAuthorized;
}
