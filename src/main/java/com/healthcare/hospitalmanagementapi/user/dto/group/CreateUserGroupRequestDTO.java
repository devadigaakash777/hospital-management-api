package com.healthcare.hospitalmanagementapi.user.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserGroupRequestDTO {

    @NotBlank
    @Size(max = 255)
    @Schema(example = "Admin Group", description = "Name of the user group")
    private String groupName;

    @Schema(description = "Department IDs assigned to this group")
    private List<UUID> departmentIds;

    private Boolean canManageDoctorSlots;

    private Boolean canManageStaff;

    private Boolean canManageGroups;

    private Boolean canExportReports;

    private Boolean canManageHealthPackages;
}
