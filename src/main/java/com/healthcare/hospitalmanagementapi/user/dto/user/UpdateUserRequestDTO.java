package com.healthcare.hospitalmanagementapi.user.dto.user;

import com.healthcare.hospitalmanagementapi.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequestDTO {

    @Size(max = 100)
    @Schema(example = "John", description = "First name of the user")
    private String firstName;

    @Size(max = 100)
    @Schema(example = "Doe", description = "Last name of the user")
    private String lastName;

    @Email
    @Size(max = 150)
    @Schema(example = "john.doe@example.com", description = "Email address of the user")
    private String email;

    @Schema(example = "STAFF", description = "Role assigned to the user")
    private Role role;

    @Schema(description = "User group ID")
    private UUID groupId;

    @Schema(description = "Updated department IDs for the user")
    private Set<UUID> departmentIds;

    @Schema(description = "Permission to manage doctor slots")
    private Boolean canManageDoctorSlots;

    @Schema(description = "Permission to manage staff")
    private Boolean canManageStaff;

    @Schema(description = "Permission to manage groups")
    private Boolean canManageGroups;

    @Schema(description = "Permission to export reports")
    private Boolean canExportReports;

    @Schema(description = "Permission to manage health packages")
    private Boolean canManageHealthPackages;
}