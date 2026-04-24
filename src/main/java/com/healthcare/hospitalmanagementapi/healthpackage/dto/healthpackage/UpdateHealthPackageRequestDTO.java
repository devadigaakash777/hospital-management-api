package com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for partially updating a health package")
public class UpdateHealthPackageRequestDTO {

    @Size(max = 150, message = "Package name must not exceed 150 characters")
    @Schema(
            description = "Updated health package name",
            example = "Executive Health Checkup"
    )
    private String packageName;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(
            description = "Updated description of the health package",
            example = "Includes advanced blood tests, ECG, MRI, and specialist consultation"
    )
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Package price must be greater than 0")
    @Schema(
            description = "Updated package price",
            example = "3499.00"
    )
    private BigDecimal packagePrice;

    @Min(value = 1, message = "Advance booking days must be at least 1")
    @Schema(
            description = "Updated number of days in advance the package can be booked",
            example = "45"
    )
    private Integer advanceBookingDays;

    @Schema(
            description = "Updated active status of the health package",
            example = "false"
    )
    private Boolean isActive;
}