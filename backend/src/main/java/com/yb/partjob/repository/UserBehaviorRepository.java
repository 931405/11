package com.yb.partjob.repository;

import com.yb.partjob.model.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    List<UserBehavior> findTop50ByStudentIdOrderByCreatedAtDesc(Long studentId);
}
