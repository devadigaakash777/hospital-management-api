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
public class UpdateDoctorTimeSlotRequestDTO {

    @Schema(example = "10:00:00", description = "Updated start time of the time slot")
    private LocalTime startTime;

    @Schema(example = "10:30:00", description = "Updated end time of the time slot")
    private LocalTime endTime;

    @Min(value = 1, message = "Total slots must be at least 1")
    @Schema(example = "12", description = "Updated total number of slots")
    private Integer totalSlots;

    @Min(value = 0, message = "Reserved slots cannot be negative")
    @Schema(
            example = "3",
            description = "Number of already reserved slots for this time slot. Must be 0 or greater"
    )
    private Integer reservedSlots;
}