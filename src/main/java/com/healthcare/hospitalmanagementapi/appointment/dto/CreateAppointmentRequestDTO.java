package com.healthcare.hospitalmanagementapi.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateAppointmentRequestDTO {

    @NotNull
    @Schema(
            description = "Patient identifier",
            example = "5f9f3fd8-c3e6-4f4f-9a77-8a3ecf5d0f0d"
    )
    private UUID patientId;

    @NotNull
    @Schema(
            description = "Doctor identifier",
            example = "e3d0dcdb-0f8d-4c98-a0df-c0a1ceef9b5a"
    )
    private UUID doctorId;

    @NotNull
    @Schema(
            description = "Doctor time slot identifier",
            example = "bb8c0a8e-5a11-44a9-8af7-8a3f9a5f9d4c"
    )
    private UUID doctorTimeSlotId;

    @NotNull
    @FutureOrPresent
    @Schema(
            description = "Appointment date",
            example = "2026-04-20"
    )
    private LocalDate appointmentDate;

    @Size(max = 5000)
    @Schema(
            description = "Optional message from patient",
            example = "Severe headache for the last two days"
    )
    private String patientMessage;

    @Schema(
            description = "Whether this appointment should be treated as VIP",
            example = "false"
    )
    private Boolean isVip;
}