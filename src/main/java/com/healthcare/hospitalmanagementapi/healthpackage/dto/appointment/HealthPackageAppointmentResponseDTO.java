package com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment;

import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing health package appointment details")
public class HealthPackageAppointmentResponseDTO {

    @Schema(description = "Health package appointment identifier")
    private UUID id;

    @Schema(description = "Patient identifier")
    private UUID patientId;

    @Schema(description = "Patient UH ID")
    private String patientUhId;

    @Schema(description = "Patient full name")
    private String patientName;

    @Schema(description = "Patient phone number")
    private String patientPhoneNumber;

    @Schema(description = "Patient email")
    private String patientEmail;

    @Schema(description = "Health package identifier")
    private UUID healthPackageId;

    @Schema(description = "Health package name")
    private String healthPackageName;

    @Schema(description = "Health package description")
    private String healthPackageDescription;

    @Schema(description = "Health package price")
    private BigDecimal healthPackagePrice;

    @Schema(description = "Health package time slot identifier")
    private UUID healthPackageTimeSlotId;

    @Schema(description = "Time slot start time")
    private LocalTime slotStartTime;

    @Schema(description = "Time slot end time")
    private LocalTime slotEndTime;

    @Schema(description = "Appointment date")
    private LocalDate appointmentDate;

    @Schema(description = "Booked appointment time")
    private LocalTime appointmentTime;

    @Schema(description = "Appointment status")
    private AppointmentStatus appointmentStatus;

    @Schema(description = "Token number assigned for the appointment")
    private Integer tokenNumber;

    @Schema(description = "Created by user identifier")
    private UUID createdByUserId;

    @Schema(description = "Created by user full name")
    private String createdByUserName;
}