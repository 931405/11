package com.yb.partjob.service.impl;

import com.yb.partjob.model.Feedback;
import com.yb.partjob.model.dto.FeedbackDTO;
import com.yb.partjob.repository.FeedbackRepository;
import com.yb.partjob.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackServiceImpl implements IFeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public void submitFeedback(Long userId, FeedbackDTO dto) {
        Feedback feedback = Feedback.builder()
                .userId(userId)
                .title(dto.getTitle())
                .content(dto.getContent())
                .contactInfo(dto.getContactInfo())
                .type(dto.getType() != null && !dto.getType().isEmpty() ? dto.getType() : "FEEDBACK")
                .status("PENDING")
                .build();
        feedbackRepository.save(feedback);
    }

    @Override
    public Page<Feedback> getMyFeedbacks(Long userId, int page, int size) {
        return feedbackRepository.findByUserId(userId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
