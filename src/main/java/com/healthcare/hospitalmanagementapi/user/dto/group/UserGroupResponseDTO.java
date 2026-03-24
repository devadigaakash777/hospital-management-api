package com.healthcare.hospitalmanagementapi.user.dto.group;

import com.healthcare.hospitalmanagementapi.department.dto.DepartmentResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "User group ID")
    private UUID id;

    @Schema(example = "Admin Group", description = "Name of the user group")
    private String groupName;

    private Boolean canManageDoctorSlots;

    private Boolean canManageStaff;

    private Boolean canManageGroups;

    private Boolean canExportReports;

    private Boolean canManageHealthPackages;

    private Set<DepartmentResponseDTO> departments;
}