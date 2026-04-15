package com.healthcare.hospitalmanagementapi.doctor.service;

import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.CreateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.DoctorTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.UpdateDoctorTimeSlotRequestDTO;

import java.util.List;
import java.util.UUID;

public interface DoctorTimeSlotService {

    DoctorTimeSlotResponseDTO create(
            UUID doctorId,
            CreateDoctorTimeSlotRequestDTO requestDTO
    );

    DoctorTimeSlotResponseDTO update(
            UUID doctorId,
            UUID slotId,
            UpdateDoctorTimeSlotRequestDTO requestDTO
    );

    void delete(
            UUID doctorId,
            UUID slotId
    );

    List<DoctorTimeSlotResponseDTO> getAllByDoctor(UUID doctorId);
}