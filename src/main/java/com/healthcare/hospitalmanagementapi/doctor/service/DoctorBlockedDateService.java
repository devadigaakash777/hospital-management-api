package com.healthcare.hospitalmanagementapi.doctor.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.CreateDoctorBlockedDateRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.DoctorBlockedDateResponseDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface DoctorBlockedDateService {

    DoctorBlockedDateResponseDTO createBlockedDate(
            UUID doctorId,
            CreateDoctorBlockedDateRequestDTO requestDTO
    );

    DoctorBlockedDateResponseDTO getBlockedDateById(UUID blockedDateId);

    PageResponse<DoctorBlockedDateResponseDTO> getBlockedDatesByDoctor(
            UUID doctorId,
            Pageable pageable
    );

    PageResponse<DoctorBlockedDateResponseDTO> getBlockedDatesByDateRange(
            UUID doctorId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    void deleteBlockedDate(UUID blockedDateId);
}
