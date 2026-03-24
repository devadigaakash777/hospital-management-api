package com.healthcare.hospitalmanagementapi.user.dto.user;

import com.healthcare.hospitalmanagementapi.department.dto.DepartmentResponseDTO;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.dto.group.UserGroupSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the user")
    private UUID id;

    @Schema(example = "John", description = "First name of the user")
    private String firstName;

    @Schema(example = "Doe", description = "Last name of the user")
    private String lastName;

    @Schema(example = "john.doe@example.com", description = "Email address of the user")
    private String email;

    @Schema(example = "STAFF", description = "Role assigned to the user")
    private Role role;

    private Boolean canManageDoctorSlots;

    private Boolean canManageStaff;

    private Boolean canManageGroups;

    private Boolean canExportReports;

    private Boolean canManageHealthPackages;

    private UserGroupSummaryDTO group;

    private Set<DepartmentResponseDTO> departments;
}