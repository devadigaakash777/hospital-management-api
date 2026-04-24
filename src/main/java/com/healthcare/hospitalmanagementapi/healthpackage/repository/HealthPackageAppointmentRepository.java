package com.healthcare.hospitalmanagementapi.healthpackage.repository;

import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HealthPackageAppointmentRepository extends JpaRepository<HealthPackageAppointment, UUID> {

    Optional<HealthPackageAppointment> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT a
            FROM HealthPackageAppointment a
            WHERE a.id = :id
            """)
    Optional<HealthPackageAppointment> findByIdIncludingDeleted(@Param("id") UUID id);

    Page<HealthPackageAppointment> findAllByIsDeletedFalseOrderByTokenNumberAsc(Pageable pageable);

    @Query(value = """
            SELECT *
            FROM health_package_appointments
            ORDER BY token_number ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM health_package_appointments
            """,
            nativeQuery = true)
    Page<HealthPackageAppointment> findAllIncludingDeleted(Pageable pageable);

    @Query(value = """
            SELECT a.*
            FROM health_package_appointments a
            JOIN health_packages hp ON hp.id = a.health_package_id AND hp.is_deleted = false
            JOIN patients p ON p.id = a.patient_id AND p.is_deleted = false
            WHERE a.is_deleted = false
              AND (:healthPackageId IS NULL OR a.health_package_id = CAST(:healthPackageId AS uuid))
              AND (:createdByUserId IS NULL OR a.created_by_user_id = CAST(:createdByUserId AS uuid))
              AND (CAST(:appointmentStatus AS appointment_status) IS NULL OR a.appointment_status = CAST(:appointmentStatus AS appointment_status))
              AND (CAST(:appointmentDate AS date) IS NULL OR a.appointment_date = CAST(:appointmentDate AS date))
              AND (
                    :search IS NULL
                    OR LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.uh_id) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(hp.package_name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
              AND (
                    :excludeCancelled = false
                    OR a.appointment_status <> 'CANCELLED'::appointment_status
              )
            ORDER BY a.token_number ASC
            """,
            countQuery = """
            SELECT COUNT(a.id)
            FROM health_package_appointments a
            JOIN health_packages hp ON hp.id = a.health_package_id AND hp.is_deleted = false
            JOIN patients p ON p.id = a.patient_id AND p.is_deleted = false
            WHERE a.is_deleted = false
              AND (:healthPackageId IS NULL OR a.health_package_id = CAST(:healthPackageId AS uuid))
              AND (:createdByUserId IS NULL OR a.created_by_user_id = CAST(:createdByUserId AS uuid))
              AND (CAST(:appointmentStatus AS appointment_status) IS NULL OR a.appointment_status = CAST(:appointmentStatus AS appointment_status))
              AND (CAST(:appointmentDate AS date) IS NULL OR a.appointment_date = CAST(:appointmentDate AS date))
              AND (
                    :search IS NULL
                    OR LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.uh_id) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(hp.package_name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
              AND (
                    :excludeCancelled = false
                    OR a.appointment_status <> 'CANCELLED'::appointment_status
              )
            """,
            nativeQuery = true)
    Page<HealthPackageAppointment> searchAppointments(
            @Param("healthPackageId") UUID healthPackageId,
            @Param("createdByUserId") UUID createdByUserId,
            @Param("appointmentStatus") String appointmentStatus,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("search") String search,
            @Param("excludeCancelled") boolean excludeCancelled,
            Pageable pageable
    );

    @Query(value = """
            SELECT COALESCE(MAX(token_number), 0) + 1
            FROM (
                SELECT a.token_number
                FROM health_package_appointments a
                WHERE a.health_package_id = :healthPackageId
                  AND a.health_package_time_slot_id = :healthPackageTimeSlotId
                  AND a.appointment_date = :appointmentDate
                FOR UPDATE
            ) AS slot_tokens
            """, nativeQuery = true)
    Integer getNextTokenNumber(
            @Param("healthPackageId") UUID healthPackageId,
            @Param("healthPackageTimeSlotId") UUID healthPackageTimeSlotId,
            @Param("appointmentDate") LocalDate appointmentDate
    );

    @Query("""
            SELECT a
            FROM HealthPackageAppointment a
            WHERE a.healthPackageTimeSlot.id = :healthPackageTimeSlotId
              AND a.isDeleted = false
            ORDER BY a.tokenNumber ASC
            """)
    Page<HealthPackageAppointment> findByHealthPackageTimeSlotId(
            @Param("healthPackageTimeSlotId") UUID healthPackageTimeSlotId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(a)
            FROM HealthPackageAppointment a
            WHERE a.healthPackage.id = :healthPackageId
              AND a.healthPackageTimeSlot.id = :healthPackageTimeSlotId
              AND a.appointmentDate = :appointmentDate
              AND a.appointmentStatus <> :cancelledStatus
            """)
    long countActiveAppointmentsForSlot(
            @Param("healthPackageId") UUID healthPackageId,
            @Param("healthPackageTimeSlotId") UUID healthPackageTimeSlotId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );

    @Query("""
            SELECT COUNT(a)
            FROM HealthPackageAppointment a
            WHERE a.healthPackage.id = :healthPackageId
              AND a.healthPackageTimeSlot.id = :healthPackageTimeSlotId
              AND a.appointmentDate = :appointmentDate
              AND a.appointmentStatus <> :cancelledStatus
              AND a.id <> :excludeAppointmentId
            """)
    long countActiveAppointmentsForSlotExcluding(
            @Param("healthPackageId") UUID healthPackageId,
            @Param("healthPackageTimeSlotId") UUID healthPackageTimeSlotId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus,
            @Param("excludeAppointmentId") UUID excludeAppointmentId
    );

    List<HealthPackageAppointment> findAllByHealthPackageIdAndAppointmentDateAndAppointmentStatusInAndIsDeletedFalse(
            UUID healthPackageId,
            LocalDate appointmentDate,
            Collection<AppointmentStatus> statuses
    );

    @Modifying
    @Query("""
            UPDATE HealthPackageAppointment a
            SET a.appointmentStatus = :cancelledStatus
            WHERE a.healthPackage.id = :healthPackageId
              AND a.appointmentDate > :lastAllowedDate
              AND a.appointmentStatus NOT IN :excludedStatuses
              AND a.isDeleted = false
            """)
    int cancelAppointmentsBeyondAdvanceBookingLimit(
            @Param("healthPackageId") UUID healthPackageId,
            @Param("lastAllowedDate") LocalDate lastAllowedDate,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus,
            @Param("excludedStatuses") Collection<AppointmentStatus> excludedStatuses
    );
}