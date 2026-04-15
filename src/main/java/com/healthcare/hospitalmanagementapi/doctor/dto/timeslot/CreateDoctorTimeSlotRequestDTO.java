package com.healthcare.hospitalmanagementapi.doctor.dto.timeslot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorTimeSlotRequestDTO {

    @NotNull(message = "Start time is required")
    @Schema(example = "09:00:00", description = "Start time of the time slot")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Schema(example = "09:30:00", description = "End time of the time slot")
    private LocalTime endTime;

    @NotNull(message = "Total slots is required")
    @Min(value = 1, message = "Total slots must be at least 1")
    @Schema(example = "10", description = "Total number of slots available in this time range")
    private Integer totalSlots;

    @NotNull(message = "Reserved slots is required")
    @Min(value = 0, message = "Reserved slots cannot be negative")
    @Schema(
            example = "3",
            description = "Number of already reserved slots for this time slot. Must be 0 or greater"
    )
    private Integer reservedSlots;
}