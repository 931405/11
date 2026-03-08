package com.yb.partjob.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BlacklistVO {
    private Long enterpriseId;
    private String companyName;
    private String industry;
    private LocalDateTime blacklistedAt;
}
