package com.healthcare.hospitalmanagementapi.appointment.entity;

import com.healthcare.hospitalmanagementapi.common.entity.BaseEntity;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(
                        name = "uk_appointments_doctor_slot_date_token",
                        columnList = "doctor_id, doctor_time_slot_id, appointment_date, token_number",
                        unique = true
                ),
                @Index(name = "idx_appointments_is_deleted", columnList = "is_deleted"),
                @Index(name = "idx_appointments_appointment_date", columnList = "appointment_date"),
                @Index(name = "idx_appointments_appointment_status", columnList = "appointment_status")
        }
)
@SQLDelete(sql = "UPDATE appointments SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_time_slot_id", nullable = false)
    private DoctorTimeSlot doctorTimeSlot;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Column(name = "doctor_designation_snapshot", nullable = false, length = 150)
    private String doctorDesignationSnapshot;

    @Column(name = "doctor_specialization_snapshot", nullable = false, length = 150)
    private String doctorSpecializationSnapshot;

    @Column(name = "department_name_snapshot", nullable = false, length = 255)
    private String departmentNameSnapshot;

    @Column(name = "patient_message", columnDefinition = "TEXT")
    private String patientMessage;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "appointment_status",
            columnDefinition = "appointment_status",
            nullable = false
    )
    private AppointmentStatus appointmentStatus;

    @Column(name = "is_vip", nullable = false)
    private Boolean isVip = false;

    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;
}