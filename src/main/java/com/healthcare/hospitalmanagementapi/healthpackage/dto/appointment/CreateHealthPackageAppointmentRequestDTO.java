package com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a health package appointment")
public class CreateHealthPackageAppointmentRequestDTO {

    @NotNull(message = "Patient ID is required")
    @Schema(
            description = "Patient identifier",
            example = "5f9f3fd8-c3e6-4f4f-9a77-8a3ecf5d0f0d"
    )
    private UUID patientId;

    @NotNull(message = "Health package ID is required")
    @Schema(
            description = "Health package identifier",
            example = "8b76d78f-21a2-4e0f-a87f-0c3470a0a5d1"
    )
    private UUID healthPackageId;

    @NotNull(message = "Health package time slot ID is required")
    @Schema(
            description = "Health package time slot identifier",
            example = "dff6b7cb-cf0a-42d2-90cb-90e7f1fce0e8"
    )
    private UUID healthPackageTimeSlotId;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date must be today or in the future")
    @Schema(
            description = "Date on which the health package appointment is booked",
            example = "2026-04-25"
    )
    private LocalDate appointmentDate;
}