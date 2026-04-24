package com.healthcare.hospitalmanagementapi.healthpackage.entity;


import com.healthcare.hospitalmanagementapi.common.entity.BaseEntity;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
        name = "health_package_appointments",
        indexes = {
                @Index(
                        name = "uk_health_package_appointments_package_slot_date_token",
                        columnList = "health_package_id, health_package_time_slot_id, appointment_date, token_number",
                        unique = true
                ),
                @Index(
                        name = "idx_health_package_appointments_health_package_id",
                        columnList = "health_package_id"
                ),
                @Index(
                        name = "idx_health_package_appointments_patient_id",
                        columnList = "patient_id"
                ),
                @Index(
                        name = "idx_health_package_appointments_appointment_date",
                        columnList = "appointment_date"
                ),
                @Index(
                        name = "idx_health_package_appointments_appointment_status",
                        columnList = "appointment_status"
                ),
                @Index(
                        name = "idx_health_package_appointments_is_deleted",
                        columnList = "is_deleted"
                )
        }
)
@SQLDelete(sql = "UPDATE health_package_appointments SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class HealthPackageAppointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_package_id", nullable = false)
    private HealthPackage healthPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_package_time_slot_id", nullable = false)
    private HealthPackageTimeSlot healthPackageTimeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "appointment_status",
            columnDefinition = "appointment_status",
            nullable = false
    )
    private AppointmentStatus appointmentStatus;

    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;
}