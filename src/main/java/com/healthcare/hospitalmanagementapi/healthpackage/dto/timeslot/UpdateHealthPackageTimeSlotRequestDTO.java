package com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
public class UpdateHealthPackageTimeSlotRequestDTO {

    @Schema(example = "10:00:00", description = "Updated start time of the time slot")
    private LocalTime startTime;

    @Schema(example = "10:30:00", description = "Updated end time of the time slot")
    private LocalTime endTime;

    @Min(value = 1, message = "Total slots must be at least 1")
    @Schema(example = "12", description = "Updated total number of patients allowed in this time slot")
    private Integer totalSlots;
}