package com.healthcare.hospitalmanagementapi.doctor.service;

import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.CreateDoctorWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.DoctorWeeklyScheduleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface DoctorWeeklyScheduleService {

    List<DoctorWeeklyScheduleResponseDTO> bulkCreate(
            UUID doctorId,
            List<CreateDoctorWeeklyScheduleRequestDTO> requestDTOs
    );

    List<DoctorWeeklyScheduleResponseDTO> bulkUpdate(
            UUID doctorId,
            List<CreateDoctorWeeklyScheduleRequestDTO> requestDTOs
    );

    void bulkDelete(
            UUID doctorId,
            List<UUID> scheduleIds
    );

    List<DoctorWeeklyScheduleResponseDTO> getAllByDoctor(UUID doctorId);

    DoctorWeeklyScheduleResponseDTO getById(UUID doctorId, UUID scheduleId);
}