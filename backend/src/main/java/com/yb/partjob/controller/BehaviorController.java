package com.yb.partjob.controller;

import com.yb.partjob.model.UserBehavior;
import com.yb.partjob.model.dto.UserBehaviorDTO;
import com.yb.partjob.model.vo.Result;
import com.yb.partjob.repository.UserBehaviorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/behavior")
public class BehaviorController {

    @Autowired
    private UserBehaviorRepository userBehaviorRepository;

    // Use StudentProfileRepository to map sys_user.id -> student_profile.id if
    // needed
    @Autowired
    private com.yb.partjob.repository.StudentProfileRepository studentProfileRepository;

    @PostMapping
    public Result<Void> logBehavior(Authentication auth, @Valid @RequestBody UserBehaviorDTO dto) {
        if (auth == null)
            return Result.success();

        Long userId = (Long) auth.getPrincipal();
        com.yb.partjob.model.StudentProfile student = studentProfileRepository.findByUserId(userId).orElse(null);
        if (student == null)
            return Result.success(); // Not a student, skip logging

        UserBehavior behavior = UserBehavior.builder()
                .studentId(student.getId())
                .jobId(dto.getJobId())
                .actionType(dto.getActionType())
                .dwellTime(dto.getDwellTime() != null ? dto.getDwellTime() : 0)
                .build();

        userBehaviorRepository.save(behavior);
        return Result.success();
    }
}
