package com.yb.partjob.repository;

import com.yb.partjob.model.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    List<JobCategory> findByParentIdOrderBySortOrder(Long parentId);

    List<JobCategory> findByStatusOrderBySortOrder(Integer status);
}
