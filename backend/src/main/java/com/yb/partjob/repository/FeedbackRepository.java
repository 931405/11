package com.yb.partjob.repository;

import com.yb.partjob.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Page<Feedback> findByUserId(Long userId, Pageable pageable);

    Page<Feedback> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);
}
