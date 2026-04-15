package com.healthcare.hospitalmanagementapi.doctor.dto.timeslot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorTimeSlotResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the time slot")
    private UUID id;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the doctor this slot belongs to")
    private UUID doctorId;

    @Schema(example = "09:00:00", description = "Start time of the slot")
    private LocalTime startTime;

    @Schema(example = "09:30:00", description = "End time of the slot")
    private LocalTime endTime;

    @Schema(example = "3", description = "Maximum patients allowed per slot")
    private Integer patientsPerSlot;

    @Schema(example = "2", description = "Number of slots currently reserved")
    private Integer reservedSlots;

    @Schema(example = "10", description = "Total number of slots in this time range")
    private Integer totalSlots;
}