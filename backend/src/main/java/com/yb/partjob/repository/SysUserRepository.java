package com.yb.partjob.repository;

import com.yb.partjob.model.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<SysUser> findByRole(String role, Pageable pageable);

    Page<SysUser> findByRoleAndStatus(String role, Integer status, Pageable pageable);

    long countByRole(String role);

    long countByRoleAndCreatedAtBetween(String role, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
