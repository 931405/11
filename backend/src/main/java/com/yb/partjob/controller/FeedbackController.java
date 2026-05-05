package com.yb.partjob.controller;

import com.yb.partjob.model.Feedback;
import com.yb.partjob.model.dto.FeedbackDTO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private IFeedbackService feedbackService;

    @PostMapping
    public Result<Void> submitFeedback(Authentication auth, @RequestBody FeedbackDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        feedbackService.submitFeedback(userId, dto);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Page<Feedback>> getMyFeedbacks(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(feedbackService.getMyFeedbacks(userId, page, size));
    }
}
