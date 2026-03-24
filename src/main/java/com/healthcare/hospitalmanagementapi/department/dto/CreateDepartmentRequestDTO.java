package com.healthcare.hospitalmanagementapi.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDepartmentRequestDTO {

    @NotBlank
    @Size(max = 255)
    @Schema(example = "Cardiology", description = "Name of the department")
    private String departmentName;
}