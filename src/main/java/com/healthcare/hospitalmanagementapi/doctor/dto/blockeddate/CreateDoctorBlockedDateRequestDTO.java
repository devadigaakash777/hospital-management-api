package com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorBlockedDateRequestDTO {

    @NotNull(message = "Blocked date is required")
    @FutureOrPresent(message = "Blocked date must be today or in the future")
    @Schema(example = "2025-12-25", description = "The date to block for the doctor (no appointments allowed)")
    private LocalDate blockedDate;

    @Size(max = 1000, message = "Block reason must not exceed 1000 characters")
    @Schema(example = "Public holiday - Christmas", description = "Optional reason for blocking this date")
    private String blockReason;
}
