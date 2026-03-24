package com.healthcare.hospitalmanagementapi.user.dto.user;

import com.healthcare.hospitalmanagementapi.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequestDTO {

    @NotBlank
    @Size(max = 100)
    @Schema(example = "John", description = "First name of the user")
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Doe", description = "Last name of the user")
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 150)
    @Schema(example = "john.doe@example.com", description = "Unique email address of the user")
    private String email;

    @NotBlank
    @Size(min = 8, max = 255)
    @Schema(example = "SecurePassword123", description = "User account password")
    private String password;

    @NotNull
    @Schema(example = "STAFF", description = "Role assigned to the user")
    private Role role;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "User group ID")
    private UUID groupId;

    @Schema(description = "Department IDs assigned to the user")
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