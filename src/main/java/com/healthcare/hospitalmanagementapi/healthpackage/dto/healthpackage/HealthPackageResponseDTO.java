package com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing health package details")
public class HealthPackageResponseDTO {

    @Schema(
            description = "Unique identifier of the health package",
            example = "8b76d78f-21a2-4e0f-a87f-0c3470a0a5d1"
    )
    private UUID id;

    @Schema(
            description = "Name of the health package",
            example = "Comprehensive Health Checkup"
    )
    private String packageName;

    @Schema(
            description = "Detailed description of the health package",
            example = "Includes blood test, ECG, chest X-ray, and physician consultation"
    )
    private String description;

    @Schema(
            description = "Price of the health package",
            example = "2499.00"
    )
    private BigDecimal packagePrice;

    @Schema(
            description = "Whether the package is currently active",
            example = "true"
    )
    private Boolean isActive;

    @Schema(
            description = "Number of days in advance the package can be booked",
            example = "30"
    )
    private Integer advanceBookingDays;
}