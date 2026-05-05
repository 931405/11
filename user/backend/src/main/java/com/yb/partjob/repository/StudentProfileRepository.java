package com.yb.partjob.repository;

import com.yb.partjob.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<StudentProfile> findByIdIn(List<Long> ids);

    @Query("SELECT p FROM StudentProfile p WHERE (p.privacyResumeOpen = true OR p.privacyResumeOpen IS NULL) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(COALESCE(p.university, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(p.major, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(p.skills, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<StudentProfile> searchPublicProfiles(@Param("keyword") String keyword, Pageable pageable);
}
