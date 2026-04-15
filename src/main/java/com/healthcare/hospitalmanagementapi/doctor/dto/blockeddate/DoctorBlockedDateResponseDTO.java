package com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorBlockedDateResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the blocked date record")
    private UUID id;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the doctor this blocked date belongs to")
    private UUID doctorId;

    @Schema(example = "2025-12-25", description = "The blocked date")
    private LocalDate blockedDate;

    @Schema(example = "Public holiday - Christmas", description = "Reason for blocking this date")
    private String blockReason;
}