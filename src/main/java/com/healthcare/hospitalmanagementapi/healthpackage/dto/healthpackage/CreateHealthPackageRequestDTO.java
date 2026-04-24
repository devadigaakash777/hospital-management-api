package com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request payload for creating a health package")
public class CreateHealthPackageRequestDTO {

    @NotBlank(message = "Package name is required")
    @Size(max = 150, message = "Package name must not exceed 150 characters")
    @Schema(
            description = "Name of the health package",
            example = "Comprehensive Health Checkup"
    )
    private String packageName;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(
            description = "Detailed description of the health package",
            example = "Includes blood test, ECG, chest X-ray, and physician consultation"
    )
    private String description;

    @NotNull(message = "Package price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Package price must be greater than 0")
    @Schema(
            description = "Price of the health package",
            example = "2499.00"
    )
    private BigDecimal packagePrice;

    @NotNull(message = "Advance booking days is required")
    @Min(value = 1, message = "Advance booking days must be at least 1")
    @Schema(
            description = "Number of days in advance the package can be booked",
            example = "30"
    )
    private Integer advanceBookingDays;

    @Schema(
            description = "Whether the package is active and available for booking",
            example = "true"
    )
    private Boolean isActive;
}