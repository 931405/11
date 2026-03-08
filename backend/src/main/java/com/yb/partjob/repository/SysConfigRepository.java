package com.yb.partjob.repository;

import com.yb.partjob.model.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {
    Optional<SysConfig> findByConfigKey(String configKey);
}
