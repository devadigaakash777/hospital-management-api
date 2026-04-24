package com.healthcare.hospitalmanagementapi.healthpackage.service;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.CreateHealthPackageWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.HealthPackageWeeklyScheduleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface HealthPackageWeeklyScheduleService {

    List<HealthPackageWeeklyScheduleResponseDTO> bulkCreate(
            UUID healthPackageId,
            List<CreateHealthPackageWeeklyScheduleRequestDTO> requestDTOs
    );

    List<HealthPackageWeeklyScheduleResponseDTO> bulkUpdate(
            UUID healthPackageId,
            List<CreateHealthPackageWeeklyScheduleRequestDTO> requestDTOs
    );

    void bulkDelete(
            UUID healthPackageId,
            List<UUID> scheduleIds
    );

    List<HealthPackageWeeklyScheduleResponseDTO> getAllByHealthPackage(UUID healthPackageId);

    HealthPackageWeeklyScheduleResponseDTO getById(UUID healthPackageId, UUID scheduleId);
}