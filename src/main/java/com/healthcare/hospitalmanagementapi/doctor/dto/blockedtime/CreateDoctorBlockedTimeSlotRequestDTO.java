package com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorBlockedTimeSlotRequestDTO {

    @NotNull(message = "Blocked date is required")
    @FutureOrPresent(message = "Blocked date must be today or in the future")
    @Schema(example = "2025-08-15", description = "The date on which specific time slots are being blocked")
    private LocalDate blockedDate;

    @NotNull(message = "Start time is required")
    @Schema(example = "14:00:00", description = "Start time of the blocked period")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Schema(example = "16:00:00", description = "End time of the blocked period")
    private LocalTime endTime;

    @Min(value = 0, message = "Reserved slots cannot be negative")
    @Schema(
            example = "2",
            description = "Optional number of already reserved appointment slots"
    )
    private Integer reservedSlots;

    @Size(max = 1000, message = "Block reason must not exceed 1000 characters")
    @Schema(example = "Doctor attending a medical conference", description = "Optional reason for blocking this time slot")
    private String blockReason;
}