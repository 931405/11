package com.yb.partjob.controller;

import com.yb.partjob.model.JobCategory;
import com.yb.partjob.model.vo.JobVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private IJobService jobService;

    @GetMapping
    public Result<Page<JobVO>> searchJobs(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.success(
                jobService.searchJobs(keyword, categoryId, location, jobType, enterpriseId, page, size, userId));
    }

    @GetMapping("/{id}")
    public Result<JobVO> getJobDetail(@PathVariable Long id, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.success(jobService.getJobDetail(id, userId));
    }

    @GetMapping("/categories")
    public Result<List<JobCategory>> getCategories() {
        return Result.success(jobService.getCategoryTree());
    }
}
