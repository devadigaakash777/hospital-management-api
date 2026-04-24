package com.healthcare.hospitalmanagementapi.healthpackage.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.CreateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.UpdateHealthPackageAppointmentRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HealthPackageAppointmentService {

    HealthPackageAppointmentResponseDTO createAppointment(CreateHealthPackageAppointmentRequestDTO request);

    HealthPackageAppointmentResponseDTO getAppointmentById(UUID appointmentId);

    PageResponse<HealthPackageAppointmentResponseDTO> getAllAppointments(Pageable pageable);

    PageResponse<HealthPackageAppointmentResponseDTO> getAllAppointmentsIncludingDeleted(Pageable pageable);

    PageResponse<HealthPackageAppointmentResponseDTO> getAppointmentsByHealthPackageId(
            UUID healthPackageId,
            Pageable pageable
    );

    PageResponse<HealthPackageAppointmentResponseDTO> getAppointmentsByHealthPackageTimeSlotId(
            UUID healthPackageTimeSlotId,
            Pageable pageable
    );

    PageResponse<HealthPackageAppointmentResponseDTO> searchAppointments(
            HealthPackageAppointmentSearchRequestDTO request,
            Pageable pageable
    );

    HealthPackageAppointmentResponseDTO updateAppointment(
            UUID appointmentId,
            UpdateHealthPackageAppointmentRequestDTO request
    );

    void deleteAppointment(UUID appointmentId);

    HealthPackageAppointmentResponseDTO restoreAppointment(UUID appointmentId);
}