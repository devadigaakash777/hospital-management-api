package com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule;

import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHealthPackageWeeklyScheduleRequestDTO {

    @NotNull(message = "Week number is required")
    @Min(value = 0, message = "Week number must be at least 0")
    @Max(value = 5, message = "Week number must not exceed 5")
    @Schema(example = "1", description = "Week number within the schedule cycle")
    private Integer weekNumber;

    @NotNull(message = "Day of week is required")
    @Schema(example = "MONDAY", description = "Day of the week the health package is available")
    private DayOfWeek dayOfWeek;
}