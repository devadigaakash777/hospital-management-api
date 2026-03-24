package com.healthcare.hospitalmanagementapi.user.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserGroupRequestDTO {

    @Size(max = 255)
    @Schema(example = "Updated Group Name", description = "Updated name of the user group")
    private String groupName;

    @Schema(description = "Updated department IDs for this group")
    private Set<UUID> departmentIds;

    private Boolean canManageDoctorSlots;

    private Boolean canManageStaff;

    private Boolean canManageGroups;

    private Boolean canExportReports;

    private Boolean canManageHealthPackages;
}