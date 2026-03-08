package com.yb.partjob.repository;

import com.yb.partjob.model.EnterpriseInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnterpriseInfoRepository extends JpaRepository<EnterpriseInfo, Long> {
    Optional<EnterpriseInfo> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Page<EnterpriseInfo> findByCertificationStatus(String certificationStatus, Pageable pageable);

    long countByCertificationStatus(String certificationStatus);
}
