package com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule;

import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
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
public class HealthPackageWeeklyScheduleResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the weekly schedule entry")
    private UUID id;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the health package this schedule entry belongs to")
    private UUID healthPackageId;

    @Schema(example = "1", description = "Week number in the schedule cycle")
    private Integer weekNumber;

    @Schema(example = "MONDAY", description = "Day of the week the health package is available")
    private DayOfWeek dayOfWeek;
}