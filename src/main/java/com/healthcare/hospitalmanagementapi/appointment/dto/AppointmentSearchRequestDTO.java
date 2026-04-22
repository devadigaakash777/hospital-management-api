package com.healthcare.hospitalmanagementapi.appointment.dto;

import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AppointmentSearchRequestDTO {

    private UUID doctorId;

    private UUID createdByUserId;

    private AppointmentStatus appointmentStatus;

    private LocalDate appointmentDate;

    private Boolean isVip;

    private String department;

    private String search;

    @Builder.Default
    private boolean excludeCancelled = false;
}