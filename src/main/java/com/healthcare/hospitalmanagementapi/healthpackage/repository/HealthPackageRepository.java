package com.healthcare.hospitalmanagementapi.healthpackage.repository;

import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface HealthPackageRepository extends JpaRepository<HealthPackage, UUID> {

    Optional<HealthPackage> findByIdAndIsDeletedFalse(UUID id);

    Page<HealthPackage> findAllByIsDeletedFalse(Pageable pageable);

    @Query(value = """
            SELECT *
            FROM health_packages
            WHERE id = :id
            """, nativeQuery = true)
    Optional<HealthPackage> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query(
            value = """
                    SELECT *
                    FROM health_packages
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM health_packages
                    """,
            nativeQuery = true
    )
    Page<HealthPackage> findAllIncludingDeleted(Pageable pageable);

    @Query("""
            SELECT hp
            FROM HealthPackage hp
            WHERE hp.isDeleted = false
              AND hp.isActive = true
              AND (
                    LOWER(hp.packageName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(hp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<HealthPackage> searchHealthPackages(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}