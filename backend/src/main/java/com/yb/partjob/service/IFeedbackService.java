package com.yb.partjob.service;

import com.yb.partjob.model.Feedback;
import com.yb.partjob.model.dto.FeedbackDTO;
import org.springframework.data.domain.Page;

public interface IFeedbackService {
    void submitFeedback(Long userId, FeedbackDTO dto);
    Page<Feedback> getMyFeedbacks(Long userId, int page, int size);
}
