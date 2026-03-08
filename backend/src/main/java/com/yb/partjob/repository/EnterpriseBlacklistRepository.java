package com.yb.partjob.repository;

import com.yb.partjob.model.EnterpriseBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnterpriseBlacklistRepository extends JpaRepository<EnterpriseBlacklist, Long> {
    List<EnterpriseBlacklist> findByStudentId(Long studentId);

    Optional<EnterpriseBlacklist> findByStudentIdAndEnterpriseId(Long studentId, Long enterpriseId);

    boolean existsByStudentIdAndEnterpriseId(Long studentId, Long enterpriseId);
}
