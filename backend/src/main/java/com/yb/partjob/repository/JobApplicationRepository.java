package com.yb.partjob.repository;

import com.yb.partjob.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByStudentId(Long studentId, Pageable pageable);

    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);

    Page<JobApplication> findByJobIdIn(List<Long> jobIds, Pageable pageable);

    Optional<JobApplication> findByStudentIdAndJobId(Long studentId, Long jobId);

    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);

    long countByJobId(Long jobId);

    long countByStatus(String status);

    List<JobApplication> findByJobIdInAndCreatedAtAfter(List<Long> jobIds, java.time.LocalDateTime date);

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
