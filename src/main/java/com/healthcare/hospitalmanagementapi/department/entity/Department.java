package com.healthcare.hospitalmanagementapi.department.entity;

import com.healthcare.hospitalmanagementapi.common.entity.BaseEntity;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE departments SET is_deleted = true WHERE id = ?")
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "department_name", nullable = false, length = 255)
    private String departmentName;

    @ManyToMany(mappedBy = "departments")
    private Set<UserGroup> userGroups = new HashSet<>();

    @ManyToMany(mappedBy = "departments")
    private Set<User> users = new HashSet<>();
}
