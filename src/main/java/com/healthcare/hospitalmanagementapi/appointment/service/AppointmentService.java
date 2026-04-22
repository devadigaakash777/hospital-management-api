package com.healthcare.hospitalmanagementapi.appointment.service;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.CreateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.UpdateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AppointmentService {

    AppointmentResponseDTO createAppointment(CreateAppointmentRequestDTO request);

    AppointmentResponseDTO getAppointmentById(UUID appointmentId);

    PageResponse<AppointmentResponseDTO> getAllAppointments(Pageable pageable);

    PageResponse<AppointmentResponseDTO> getAllAppointmentsIncludingDeleted(Pageable pageable);

    PageResponse<AppointmentResponseDTO> getAppointmentsByDoctorId(
            UUID doctorId,
            Pageable pageable
    );

    PageResponse<AppointmentResponseDTO> getAppointmentsByCreatedUserId(
            UUID createdByUserId,
            Pageable pageable
    );

    PageResponse<AppointmentResponseDTO> searchAppointments(
            AppointmentSearchRequestDTO request,
            Pageable pageable
    );

    AppointmentResponseDTO updateAppointment(
            UUID appointmentId,
            UpdateAppointmentRequestDTO request
    );

    void deleteAppointment(UUID appointmentId);

    AppointmentResponseDTO restoreAppointment(UUID appointmentId);

    PageResponse<AppointmentResponseDTO> getAppointmentsByDoctorTimeSlotId(
            UUID doctorTimeSlotId,
            Pageable pageable
    );
}