package com.yb.partjob.repository;

import com.yb.partjob.model.MatchScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchScoreRepository extends JpaRepository<MatchScore, Long> {
    Page<MatchScore> findByStudentIdOrderByTotalScoreDesc(Long studentId, Pageable pageable);

    Optional<MatchScore> findByStudentIdAndJobId(Long studentId, Long jobId);

    void deleteByStudentId(Long studentId);

    void deleteByJobId(Long jobId);
}
