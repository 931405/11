package com.yb.partjob.controller;

import com.yb.partjob.model.EnterpriseInfo;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.repository.EnterpriseInfoRepository;
import com.yb.partjob.repository.JobPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enterprises")
public class EnterprisePublicController {

    @Autowired
    private EnterpriseInfoRepository enterpriseInfoRepository;

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @GetMapping("/{id}")
    public Result<EnterpriseInfo> getEnterprisePublicInfo(@PathVariable Long id) {
        EnterpriseInfo info = enterpriseInfoRepository.findById(id).orElse(null);
        if (info == null) {
            return Result.error("Enterprise not found");
        }
        // Do not expose sensitive contact info to public
        info.setContactPerson(null);
        info.setContactPhone(null);
        info.setBusinessLicense(null);
        info.setCertificationRemark(null);
        return Result.success(info);
    }

    @GetMapping("/{id}/job-count")
    public Result<Long> getEnterpriseJobCount(@PathVariable Long id) {
        if (!enterpriseInfoRepository.existsById(id)) {
            return Result.error("Enterprise not found");
        }
        long count = jobPositionRepository.countByEnterpriseIdAndStatus(id, "OPEN");
        return Result.success(count);
    }
}
