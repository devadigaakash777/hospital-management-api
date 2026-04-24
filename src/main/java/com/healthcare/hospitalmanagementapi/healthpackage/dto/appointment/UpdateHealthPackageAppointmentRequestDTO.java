package com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment;

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
@Schema(description = "Request payload for partially updating a health package appointment")
public class UpdateHealthPackageAppointmentRequestDTO {

    @Schema(
            description = "Updated appointment status",
            example = "CONFIRMED"
    )
    private AppointmentStatus appointmentStatus;
}