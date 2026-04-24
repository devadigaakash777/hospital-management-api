package com.healthcare.hospitalmanagementapi.healthpackage.entity;

import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "health_package_weekly_schedules",
        indexes = {
                @Index(
                        name = "idx_health_package_weekly_schedules_health_package_id",
                        columnList = "health_package_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_health_package_weekly_schedule_package_week_day",
                        columnNames = {
                                "health_package_id",
                                "week_number",
                                "day_of_week"
                        }
                )
        }
)
public class HealthPackageWeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_package_id", nullable = false)
    private HealthPackage healthPackage;

    @Min(0)
    @Max(5)
    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "day_of_week",
            columnDefinition = "day_of_week",
            nullable = false
    )
    private DayOfWeek dayOfWeek;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}