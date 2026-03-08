package com.yb.partjob.controller;

import com.yb.partjob.model.dto.InterviewInvitationDTO;
import com.yb.partjob.model.vo.InterviewInvitationVO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IEnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enterprise/interviews")
public class InterviewController {

    @Autowired
    private IEnterpriseService enterpriseService;

    @PostMapping
    public Result<Void> sendInvitation(Authentication auth, @RequestBody InterviewInvitationDTO dto) {
        Long enterpriseUserId = (Long) auth.getPrincipal();
        enterpriseService.sendInterviewInvitation(enterpriseUserId, dto);
        return Result.success();
    }

    @GetMapping
    public Result<Page<InterviewInvitationVO>> getMyInvitations(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long enterpriseUserId = (Long) auth.getPrincipal();
        return Result.success(enterpriseService.getEnterpriseInvitations(enterpriseUserId, page, size));
    }
}
