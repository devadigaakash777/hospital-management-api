package com.healthcare.hospitalmanagementapi.doctor.repository;

import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByIdAndIsDeletedFalse(UUID id);

    Page<Doctor> findAllByIsDeletedFalse(Pageable pageable);

    @Query(value = """
    SELECT *
    FROM doctors
    WHERE id = :id
    """, nativeQuery = true)
    Optional<Doctor> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query(
            value = """
                SELECT *
                FROM doctors
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM doctors
                """,
            nativeQuery = true
    )
    Page<Doctor> findAllIncludingDeleted(Pageable pageable);

    @Query("""
    SELECT d
    FROM Doctor d
    WHERE d.isDeleted = false
      AND (
            LOWER(CONCAT(d.user.firstName, ' ', d.user.lastName))
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(d.user.firstName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(d.user.lastName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(d.department.departmentName)
                LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
""")
    Page<Doctor> searchDoctors(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Doctor> findByDepartmentIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID departmentId);
}
