package com.healthcare.hospitalmanagementapi.appointment.dto;

import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAppointmentRequestDTO {

    @Schema(
            description = "Current appointment status",
            example = "CONFIRMED"
    )
    private AppointmentStatus appointmentStatus;

    @Schema(
            description = "Updated patient message",
            example = "Pain has increased since yesterday"
    )
    private String patientMessage;

    @Schema(
            description = "Whether this appointment should be treated as VIP",
            example = "true"
    )
    private Boolean isVip;
}