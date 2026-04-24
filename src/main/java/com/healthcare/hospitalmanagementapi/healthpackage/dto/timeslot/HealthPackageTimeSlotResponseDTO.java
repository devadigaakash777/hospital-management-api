package com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthPackageTimeSlotResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the time slot")
    private UUID id;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the health package this slot belongs to")
    private UUID healthPackageId;

    @Schema(example = "09:00:00", description = "Start time of the slot")
    private LocalTime startTime;

    @Schema(example = "09:30:00", description = "End time of the slot")
    private LocalTime endTime;

    @Schema(example = "10", description = "Total number of patients allowed in this time slot")
    private Integer totalSlots;
}