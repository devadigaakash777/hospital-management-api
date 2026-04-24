package com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment;

import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Search and filter criteria for health package appointments")
public class HealthPackageAppointmentSearchRequestDTO {

    @Schema(description = "Filter by health package ID")
    private UUID healthPackageId;

    @Schema(description = "Filter by the user who created the appointment")
    private UUID createdByUserId;

    @Schema(description = "Filter by appointment status")
    private AppointmentStatus appointmentStatus;

    @Schema(description = "Filter by appointment date", example = "2026-04-25")
    private LocalDate appointmentDate;

    @Schema(description = "Search by patient name, UH ID, or package name")
    private String search;

    @Schema(description = "Exclude cancelled appointments", defaultValue = "false")
    private boolean excludeCancelled;
}