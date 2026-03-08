package com.yb.partjob.repository;

import com.yb.partjob.model.JobPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {

        Page<JobPosition> findByStatus(String status, Pageable pageable);

        Page<JobPosition> findByEnterpriseId(Long enterpriseId, Pageable pageable);

        Page<JobPosition> findByEnterpriseIdAndStatus(Long enterpriseId, String status, Pageable pageable);

        @Query("SELECT j FROM JobPosition j, EnterpriseInfo e WHERE j.enterpriseId = e.id AND j.status = 'OPEN' " +
                        "AND (:keyword IS NULL OR j.title LIKE %:keyword% OR j.description LIKE %:keyword% OR e.companyName LIKE %:keyword%) "
                        +
                        "AND (:categoryId IS NULL OR j.categoryId = :categoryId) " +
                        "AND (:jobType IS NULL OR j.jobType = :jobType) " +
                        "AND (:enterpriseId IS NULL OR j.enterpriseId = :enterpriseId) " +
                        "AND (:location IS NULL OR j.workLocation LIKE %:location%)")
        Page<JobPosition> searchJobs(@Param("keyword") String keyword,
                        @Param("categoryId") Long categoryId,
                        @Param("location") String location,
                        @Param("jobType") String jobType,
                        @Param("enterpriseId") Long enterpriseId,
                        Pageable pageable);

        List<JobPosition> findByIdIn(List<Long> ids);

        long countByStatus(String status);

        long countByEnterpriseId(Long enterpriseId);
}
