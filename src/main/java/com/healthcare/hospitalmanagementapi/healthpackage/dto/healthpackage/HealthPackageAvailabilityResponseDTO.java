package com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.HealthPackageWeeklyScheduleResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Basic health package availability information")
public class HealthPackageAvailabilityResponseDTO {

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

    @Schema(description = "Weekly schedules of the health package")
    private List<HealthPackageWeeklyScheduleResponseDTO> weeklySchedules;

    @Schema(
            example = "30",
            description = "Number of days in advance the health package can be booked"
    )
    private Integer advanceBookingDays;
}