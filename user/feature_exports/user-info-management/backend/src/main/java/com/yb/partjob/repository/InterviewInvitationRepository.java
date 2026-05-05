package com.yb.partjob.repository;

import com.yb.partjob.model.InterviewInvitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewInvitationRepository extends JpaRepository<InterviewInvitation, Long> {
    Page<InterviewInvitation> findByEnterpriseId(Long enterpriseId, Pageable pageable);

    Page<InterviewInvitation> findByStudentId(Long studentId, Pageable pageable);

    boolean existsByApplicationId(Long applicationId);

    InterviewInvitation findByApplicationId(Long applicationId);
}
