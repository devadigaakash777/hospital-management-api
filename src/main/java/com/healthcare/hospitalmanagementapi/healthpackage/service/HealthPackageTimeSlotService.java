package com.healthcare.hospitalmanagementapi.healthpackage.service;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.CreateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.HealthPackageTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.UpdateHealthPackageTimeSlotRequestDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HealthPackageTimeSlotService {

    HealthPackageTimeSlotResponseDTO create(
            UUID healthPackageId,
            CreateHealthPackageTimeSlotRequestDTO requestDTO
    );

    HealthPackageTimeSlotResponseDTO update(
            UUID healthPackageId,
            UUID slotId,
            UpdateHealthPackageTimeSlotRequestDTO requestDTO
    );

    void delete(
            UUID healthPackageId,
            UUID slotId
    );

    List<HealthPackageTimeSlotResponseDTO> getAllByHealthPackage(UUID healthPackageId);

    List<HealthPackageTimeSlotResponseDTO> getAvailableSlots(
            UUID healthPackageId,
            LocalDate appointmentDate
    );
}