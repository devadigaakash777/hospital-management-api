package com.healthcare.hospitalmanagementapi.appointment.dto;

import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDTO {

    @Schema(description = "Appointment identifier")
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

    @Schema(description = "Doctor identifier")
    private UUID doctorId;

    @Schema(description = "Doctor full name")
    private String doctorName;

    @Schema(description = "Doctor time slot identifier")
    private UUID doctorTimeSlotId;

    @Schema(description = "Department name")
    private String departmentName;

    @Schema(description = "Doctor designation")
    private String doctorDesignation;

    @Schema(description = "Doctor specialization")
    private String doctorSpecialization;

    @Schema(description = "Appointment date")
    private LocalDate appointmentDate;

    @Schema(description = "Booked appointment time")
    private LocalTime appointmentTime;

    @Schema(description = "Time slot start time")
    private LocalTime slotStartTime;

    @Schema(description = "Time slot end time")
    private LocalTime slotEndTime;

    @Schema(description = "Appointment status")
    private AppointmentStatus appointmentStatus;

    @Schema(description = "Patient message")
    private String patientMessage;

    @Schema(description = "Whether appointment is VIP")
    private Boolean isVip;

    @Schema(description = "Token number")
    private Integer tokenNumber;

    @Schema(description = "Created by user identifier")
    private UUID createdByUserId;

    @Schema(description = "Created by user full name")
    private String createdByUserName;
}