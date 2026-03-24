package com.healthcare.hospitalmanagementapi.user.entity;
import com.healthcare.hospitalmanagementapi.common.entity.BaseEntity;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_role", columnList = "role"),
                @Index(name = "idx_users_group_id", columnList = "group_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 150)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private UserGroup group;

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
            name = "user_departments",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> departments = new HashSet<>();
}