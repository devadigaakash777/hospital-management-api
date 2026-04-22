package com.healthcare.hospitalmanagementapi.appointment.service.impl;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.mapper.AppointmentMapper;
import com.healthcare.hospitalmanagementapi.appointment.repository.AppointmentRepository;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentQueryService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    public PageResponse<AppointmentResponseDTO> searchAppointments(
            AppointmentSearchRequestDTO request,
            Pageable pageable
    ) {
        Page<AppointmentResponseDTO> page = appointmentRepository.searchAppointments(
                        request.getDoctorId(),
                        request.getCreatedByUserId(),
                        request.getAppointmentStatus() != null
                                ? request.getAppointmentStatus().name()
                                : null,
                        request.getAppointmentDate(),
                        request.getIsVip(),
                        request.getDepartment(),
                        request.getSearch(),
                        request.isExcludeCancelled(),
                        pageable
                )
                .map(appointmentMapper::toResponseDTO);

        return PageResponse.of(page);
    }
}