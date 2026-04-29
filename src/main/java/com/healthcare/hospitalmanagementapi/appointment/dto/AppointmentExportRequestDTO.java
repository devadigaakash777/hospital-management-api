package com.healthcare.hospitalmanagementapi.appointment.dto;

import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AppointmentExportRequestDTO {

    @Schema(
            description = "Filter by doctor ID",
            example = "e3d0dcdb-0f8d-4c98-a0df-c0a1ceef9b5a"
    )
    private UUID doctorId;

    @Schema(
            description = "Filter by the user who created the appointment",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    )
    private UUID createdByUserId;

    @Schema(
            description = "Filter by appointment status",
            example = "CONFIRMED"
    )
    private AppointmentStatus appointmentStatus;

    @Schema(
            description = "Start date of the date range filter (inclusive). Defaults to today if not provided.",
            example = "2026-04-01"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(
            description = "End date of the date range filter (inclusive). Defaults to today if not provided.",
            example = "2026-04-29"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}