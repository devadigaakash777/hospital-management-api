package com.healthcare.hospitalmanagementapi.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the department")
    private UUID id;

    @Schema(example = "Cardiology", description = "Name of the department")
    private String departmentName;
}