package com.healthcare.hospitalmanagementapi.doctor.entity;

import com.healthcare.hospitalmanagementapi.common.entity.BaseEntity;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "doctors",
        indexes = {
                @Index(name = "idx_doctors_department_id", columnList = "department_id")
        }
)
@SQLDelete(sql = "UPDATE doctors SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Doctor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "qualification", nullable = false, length = 255)
    private String qualification;

    @Column(name = "designation", nullable = false, length = 150)
    private String designation;

    @Column(name = "specialization", nullable = false, length = 150)
    private String specialization;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "advance_booking_days", nullable = false)
    private Integer advanceBookingDays;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;
}
