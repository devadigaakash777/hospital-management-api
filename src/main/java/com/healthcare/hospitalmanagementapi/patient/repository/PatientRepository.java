package com.healthcare.hospitalmanagementapi.patient.repository;

import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByIdAndIsDeletedFalse(UUID id);

    Optional<Patient> findByUhIdAndIsDeletedFalse(String uhId);

    boolean existsByUhIdAndIsDeletedFalse(String uhId);

    Page<Patient> findAllByIsDeletedFalse(Pageable pageable);

    @Query("""
            SELECT p
            FROM Patient p
            WHERE p.isDeleted = false
            AND (
                LOWER(p.uhId) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<Patient> search(String search, Pageable pageable);


    @Query(
            value = """
                SELECT uh_id
                FROM patients
                WHERE uh_id LIKE CONCAT(:prefix, '%')
                ORDER BY uh_id DESC
                LIMIT 1
                """,
            nativeQuery = true
    )
    Optional<String> findLatestUhIdByPrefix(String prefix);

    @Query(
            value = """
                    SELECT *
                    FROM patients
                    WHERE id = :id
                    """,
            nativeQuery = true
    )
    Optional<Patient> findByIdIncludingDeleted(UUID id);
}