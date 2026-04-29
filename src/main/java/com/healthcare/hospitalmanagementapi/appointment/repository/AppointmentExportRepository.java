package com.healthcare.hospitalmanagementapi.appointment.repository;

import com.healthcare.hospitalmanagementapi.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository dedicated to export / reporting queries.
 *
 * <p>Kept separate from {@link AppointmentRepository} to keep that interface
 * focused on operational queries (paginated lookups, counts, bulk updates)
 * while this one owns the unbounded, filter-driven reads used only for
 * report generation.
 */
public interface AppointmentExportRepository extends JpaRepository<Appointment, UUID> {

    /**
     * Returns all non-deleted appointments that match every supplied filter.
     * A {@code null} parameter value means "no filter on that field".
     *
     * <p>Results are ordered VIP-first, then by token number ascending —
     * the same order used in the operational list views.
     *
     * @param doctorId        optional doctor filter
     * @param createdByUserId optional creator-user filter
     * @param appointmentStatus optional status filter (enum name as String, e.g. {@code "CONFIRMED"})
     * @param startDate       inclusive lower bound on {@code appointment_date}
     * @param endDate         inclusive upper bound on {@code appointment_date}
     * @return matching appointments with all lazy associations fetched
     */
    @Query(value = """
            SELECT a.*
            FROM appointments a
            JOIN doctor_time_slots dts ON dts.id = a.doctor_time_slot_id
            JOIN doctors           d   ON d.id   = dts.doctor_id AND d.is_deleted  = false
            JOIN users             u   ON u.id   = d.user_id
            JOIN patients          p   ON p.id   = a.patient_id  AND p.is_deleted  = false
            JOIN users             cu  ON cu.id  = a.created_by_user_id
            WHERE a.is_deleted = false
              AND a.appointment_date BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date)
              AND (:doctorId        IS NULL OR a.doctor_id          = CAST(:doctorId        AS uuid))
              AND (:createdByUserId IS NULL OR a.created_by_user_id = CAST(:createdByUserId AS uuid))
              AND (
                    CAST(:appointmentStatus AS appointment_status) IS NULL
                    OR a.appointment_status = CAST(:appointmentStatus AS appointment_status)
              )
            ORDER BY a.is_vip DESC, a.token_number ASC
            """,
            nativeQuery = true)
    List<Appointment> findForExport(
            @Param("doctorId")           UUID      doctorId,
            @Param("createdByUserId")    UUID      createdByUserId,
            @Param("appointmentStatus")  String    appointmentStatus,
            @Param("startDate")          LocalDate startDate,
            @Param("endDate")            LocalDate endDate
    );
}