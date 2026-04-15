package com.healthcare.hospitalmanagementapi.doctor.entity;

import com.healthcare.hospitalmanagementapi.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "doctor_blocked_time_slots",
        indexes = {
                @Index(name = "idx_doctor_blocked_time_slots_doctor_id", columnList = "doctor_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doctor_blocked_time_slot_doctor_date_time",
                        columnNames = {"doctor_id", "blocked_date", "start_time", "end_time"}
                )
        }
)
@SQLDelete(sql = "UPDATE doctor_blocked_time_slots SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class DoctorBlockedTimeSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "batch_id")
    private UUID batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "blocked_date", nullable = false)
    private LocalDate blockedDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "reserved_slots", nullable = false)
    @Builder.Default
    private Integer reservedSlots = 0;

    @Column(name = "block_reason", length = 1000)
    private String blockReason;
}
