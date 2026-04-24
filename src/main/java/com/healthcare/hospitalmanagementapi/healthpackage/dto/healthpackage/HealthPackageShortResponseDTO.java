package com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Basic health package information used for dropdowns and search suggestions")
public class HealthPackageShortResponseDTO {

    @Schema(
            example = "8b76d78f-21a2-4e0f-a87f-0c3470a0a5d1",
            description = "Unique identifier of the health package"
    )
    private UUID id;

    @Schema(
            example = "Comprehensive Health Checkup",
            description = "Name of the health package"
    )
    private String packageName;
}