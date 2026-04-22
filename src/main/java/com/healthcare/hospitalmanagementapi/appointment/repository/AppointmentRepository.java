package com.healthcare.hospitalmanagementapi.appointment.repository;

import com.healthcare.hospitalmanagementapi.appointment.entity.Appointment;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT a
            FROM Appointment a
            WHERE a.id = :id
            """)
    Optional<Appointment> findByIdIncludingDeleted(@Param("id") UUID id);

    Page<Appointment> findAllByIsDeletedFalseOrderByIsVipDescTokenNumberAsc(Pageable pageable);

    @Query(value = """
        SELECT a.*
        FROM appointments a
        JOIN doctor_time_slots dts ON dts.id = a.doctor_time_slot_id
        JOIN doctors d ON d.id = dts.doctor_id AND d.is_deleted = false
        JOIN users u ON u.id = d.user_id
        JOIN patients p ON p.id = a.patient_id AND p.is_deleted = false
        WHERE a.is_deleted = false
          AND (:doctorId IS NULL OR a.doctor_id = CAST(:doctorId AS uuid))
          AND (:createdByUserId IS NULL OR a.created_by_user_id = CAST(:createdByUserId AS uuid))
          AND (CAST(:appointmentStatus AS appointment_status) IS NULL OR a.appointment_status = CAST(:appointmentStatus AS appointment_status))
          AND (CAST(:appointmentDate AS date) IS NULL OR a.appointment_date = CAST(:appointmentDate AS date))
          AND (:isVip IS NULL OR a.is_vip = CAST(:isVip AS boolean))
          AND (:department IS NULL OR LOWER(a.department_name_snapshot) LIKE LOWER(CONCAT('%', :department, '%')))
          AND (
                :search IS NULL
                OR LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.uh_id) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.department_name_snapshot) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (
                :excludeCancelled = false
                OR a.appointment_status <> 'CANCELLED'::appointment_status
          )
        ORDER BY a.is_vip DESC, a.token_number ASC
        """,
            countQuery = """
        SELECT COUNT(a.id)
        FROM appointments a
        JOIN doctor_time_slots dts ON dts.id = a.doctor_time_slot_id
        JOIN doctors d ON d.id = dts.doctor_id AND d.is_deleted = false
        JOIN users u ON u.id = d.user_id
        JOIN patients p ON p.id = a.patient_id AND p.is_deleted = false
        WHERE a.is_deleted = false
          AND (:doctorId IS NULL OR a.doctor_id = CAST(:doctorId AS uuid))
          AND (:createdByUserId IS NULL OR a.created_by_user_id = CAST(:createdByUserId AS uuid))
          AND (CAST(:appointmentStatus AS appointment_status) IS NULL OR a.appointment_status = CAST(:appointmentStatus AS appointment_status))
          AND (CAST(:appointmentDate AS date) IS NULL OR a.appointment_date = CAST(:appointmentDate AS date))
          AND (:isVip IS NULL OR a.is_vip = CAST(:isVip AS boolean))
          AND (:department IS NULL OR LOWER(a.department_name_snapshot) LIKE LOWER(CONCAT('%', :department, '%')))
          AND (
                :search IS NULL
                OR LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.uh_id) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.department_name_snapshot) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (
                :excludeCancelled = false
                OR a.appointment_status <> 'CANCELLED'::appointment_status
          )
        """,
            nativeQuery = true)
    Page<Appointment> searchAppointments(
            @Param("doctorId") UUID doctorId,
            @Param("createdByUserId") UUID createdByUserId,
            @Param("appointmentStatus") String appointmentStatus,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("isVip") Boolean isVip,
            @Param("department") String department,
            @Param("search") String search,
            @Param("excludeCancelled") boolean excludeCancelled,
            Pageable pageable
    );

    @Query(
            value = """
                SELECT *
                FROM appointments
                ORDER BY is_vip DESC, token_number ASC
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM appointments
                """,
            nativeQuery = true
    )
    Page<Appointment> findAllIncludingDeleted(Pageable pageable);

    @Query(value = """
    SELECT COALESCE(MAX(token_number), 0) + 1
    FROM (
        SELECT a.token_number
        FROM appointments a
        WHERE a.doctor_id = :doctorId
          AND a.doctor_time_slot_id = :doctorTimeSlotId
          AND a.appointment_date = :appointmentDate
        FOR UPDATE
    ) AS slot_tokens
    """, nativeQuery = true)
    Integer getNextTokenNumber(
            @Param("doctorId") UUID doctorId,
            @Param("doctorTimeSlotId") UUID doctorTimeSlotId,
            @Param("appointmentDate") LocalDate appointmentDate
    );

    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.doctorTimeSlot.id = :doctorTimeSlotId
          AND a.isDeleted = false
        ORDER BY a.isVip DESC, a.tokenNumber ASC
        """)
    Page<Appointment> findByDoctorTimeSlotId(
            @Param("doctorTimeSlotId") UUID doctorTimeSlotId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(a)
            FROM Appointment a
            WHERE a.doctorId = :doctorId
              AND a.doctorTimeSlot.id = :doctorTimeSlotId
              AND a.appointmentDate = :appointmentDate
              AND a.appointmentStatus <> :cancelledStatus
              AND a.isVip = :isVip
            """)
    long countActiveAppointmentsForSlot(
            @Param("doctorId") UUID doctorId,
            @Param("doctorTimeSlotId") UUID doctorTimeSlotId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("isVip") boolean isVip,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );

    @Query("""
            SELECT COUNT(a)
            FROM Appointment a
            WHERE a.doctorId = :doctorId
              AND a.doctorTimeSlot.id = :doctorTimeSlotId
              AND a.appointmentDate = :appointmentDate
              AND a.appointmentStatus <> :cancelledStatus
              AND a.isVip = :isVip
              AND a.id <> :excludeAppointmentId
            """)
    long countActiveAppointmentsForSlotExcluding(
            @Param("doctorId") UUID doctorId,
            @Param("doctorTimeSlotId") UUID doctorTimeSlotId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("isVip") boolean isVip,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus,
            @Param("excludeAppointmentId") UUID excludeAppointmentId
    );

    List<Appointment> findAllByDoctorIdAndAppointmentDateAndAppointmentStatusInAndIsDeletedFalse(
            UUID doctorId,
            LocalDate appointmentDate,
            Collection<AppointmentStatus> statuses
    );

    List<Appointment> findAllByDoctorIdAndAppointmentDateAndAppointmentStatusInAndAppointmentTimeGreaterThanEqualAndAppointmentTimeLessThanAndIsDeletedFalse(
            UUID doctorId,
            LocalDate appointmentDate,
            Collection<AppointmentStatus> statuses,
            LocalTime startTime,
            LocalTime endTime
    );
}