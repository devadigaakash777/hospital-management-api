package com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHealthPackageTimeSlotRequestDTO {

    @NotNull(message = "Start time is required")
    @Schema(example = "09:00:00", description = "Start time of the time slot")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Schema(example = "09:30:00", description = "End time of the time slot")
    private LocalTime endTime;

    @NotNull(message = "Total slots is required")
    @Min(value = 1, message = "Total slots must be at least 1")
    @Schema(example = "10", description = "Total number of patients allowed in this time slot")
    private Integer totalSlots;
}