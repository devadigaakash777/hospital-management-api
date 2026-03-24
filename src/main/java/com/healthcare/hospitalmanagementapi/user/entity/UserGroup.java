package com.healthcare.hospitalmanagementapi.user.entity;

import com.healthcare.hospitalmanagementapi.common.entity.BaseEntity;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE user_groups SET is_deleted = true WHERE id = ?")
public class UserGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;

    @Column(name = "can_manage_doctor_slots")
    private Boolean canManageDoctorSlots = false;

    @Column(name = "can_manage_staff")
    private Boolean canManageStaff = false;

    @Column(name = "can_manage_groups")
    private Boolean canManageGroups = false;

    @Column(name = "can_export_reports")
    private Boolean canExportReports = false;

    @Column(name = "can_manage_health_packages")
    private Boolean canManageHealthPackages = false;

    @ManyToMany
    @JoinTable(
            name = "group_departments",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> departments = new HashSet<>();
}