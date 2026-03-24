package com.healthcare.hospitalmanagementapi.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDepartmentRequestDTO {

    @Size(max = 255)
    @Schema(example = "Neurology", description = "Updated name of the department")
    private String departmentName;
}