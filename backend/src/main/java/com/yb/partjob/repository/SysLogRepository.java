package com.yb.partjob.repository;

import com.yb.partjob.model.SysLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLogRepository extends JpaRepository<SysLog, Long>, JpaSpecificationExecutor<SysLog> {
    Page<SysLog> findByActionContaining(String action, Pageable pageable);
}
