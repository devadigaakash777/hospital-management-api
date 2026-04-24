package com.healthcare.hospitalmanagementapi.healthpackage.service.impl;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageAppointmentMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageAppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthPackageAppointmentQueryService {

    private final HealthPackageAppointmentRepository appointmentRepository;
    private final HealthPackageAppointmentMapper appointmentMapper;

    public PageResponse<HealthPackageAppointmentResponseDTO> searchAppointments(
            HealthPackageAppointmentSearchRequestDTO request,
            Pageable pageable
    ) {
        Page<HealthPackageAppointmentResponseDTO> page = appointmentRepository.searchAppointments(
                        request.getHealthPackageId(),
                        request.getCreatedByUserId(),
                        request.getAppointmentStatus() != null
                                ? request.getAppointmentStatus().name()
                                : null,
                        request.getAppointmentDate(),
                        request.getSearch(),
                        request.isExcludeCancelled(),
                        pageable
                )
                .map(appointmentMapper::toResponseDTO);

        return PageResponse.of(page);
    }
}