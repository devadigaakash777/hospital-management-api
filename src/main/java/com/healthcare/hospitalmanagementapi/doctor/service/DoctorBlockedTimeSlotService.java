package com.healthcare.hospitalmanagementapi.doctor.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.CreateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.DoctorBlockedTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.UpdateDoctorBlockedTimeSlotRequestDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface DoctorBlockedTimeSlotService {

    DoctorBlockedTimeSlotResponseDTO createBlockedTimeSlot(
            UUID doctorId,
            CreateDoctorBlockedTimeSlotRequestDTO requestDTO
    );

    DoctorBlockedTimeSlotResponseDTO getBlockedTimeSlotById(UUID blockedTimeSlotId);

    PageResponse<DoctorBlockedTimeSlotResponseDTO> getBlockedTimeSlotsByDoctor(
            UUID doctorId,
            Pageable pageable
    );

    PageResponse<DoctorBlockedTimeSlotResponseDTO> getBlockedTimeSlotsByDate(
            UUID doctorId,
            LocalDate blockedDate,
            Pageable pageable
    );

    DoctorBlockedTimeSlotResponseDTO updateBlockedTimeSlot(
            UUID blockedTimeSlotId,
            UpdateDoctorBlockedTimeSlotRequestDTO requestDTO
    );

    void deleteBlockedTimeSlot(UUID blockedTimeSlotId);
}
