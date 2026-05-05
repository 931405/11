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

    @Query("SELECT p FROM StudentProfile p WHERE p.privacyResumeOpen = true AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.university) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.major) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.skills) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:educationLevel IS NULL OR :educationLevel = '' OR p.educationLevel = :educationLevel) AND " +
            "(:major IS NULL OR :major = '' OR p.major = :major) AND " +
            "(:expectedLocation IS NULL OR :expectedLocation = '' OR p.expectedLocation = :expectedLocation)")
    Page<StudentProfile> searchPublicProfiles(@Param("keyword") String keyword,
            @Param("educationLevel") String educationLevel,
            @Param("major") String major,
            @Param("expectedLocation") String expectedLocation,
            Pageable pageable);

    @Query("SELECT DISTINCT p.major FROM StudentProfile p " +
            "WHERE p.privacyResumeOpen = true AND p.major IS NOT NULL AND p.major <> '' ORDER BY p.major")
    List<String> findDistinctPublicMajors();

    @Query("SELECT DISTINCT p.educationLevel FROM StudentProfile p " +
            "WHERE p.privacyResumeOpen = true AND p.educationLevel IS NOT NULL AND p.educationLevel <> ''")
    List<String> findDistinctPublicEducationLevels();

    @Query("SELECT DISTINCT p.expectedLocation FROM StudentProfile p " +
            "WHERE p.privacyResumeOpen = true AND p.expectedLocation IS NOT NULL AND p.expectedLocation <> '' ORDER BY p.expectedLocation")
    List<String> findDistinctPublicLocations();
}
