package com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorBlockedTimeSlotResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the blocked time slot record")
    private UUID id;

    @Schema(example = "7bc91a34-1234-4567-b3fc-9a8b7c6d5e4f", description = "Batch ID grouping multiple blocked slots created together")
    private UUID batchId;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the doctor this blocked slot belongs to")
    private UUID doctorId;

    @Schema(example = "2025-08-15", description = "The blocked date")
    private LocalDate blockedDate;

    @Schema(example = "14:00:00", description = "Start time of the blocked period")
    private LocalTime startTime;

    @Schema(example = "16:00:00", description = "End time of the blocked period")
    private LocalTime endTime;

    @Schema(example = "2", description = "Number of slots that were already reserved when this block was applied")
    private Integer reservedSlots;

    @Schema(example = "Doctor attending a medical conference", description = "Reason for blocking this time slot")
    private String blockReason;
}
