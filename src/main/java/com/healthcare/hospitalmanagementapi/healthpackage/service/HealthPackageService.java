package com.healthcare.hospitalmanagementapi.healthpackage.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.CreateHealthPackageRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageAvailabilityResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageShortResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.UpdateHealthPackageRequestDTO;

import java.util.UUID;

public interface HealthPackageService {

    HealthPackageResponseDTO createHealthPackage(CreateHealthPackageRequestDTO dto);

    HealthPackageResponseDTO getHealthPackageById(UUID id);

    PageResponse<HealthPackageResponseDTO> getAllHealthPackages(int page, int size);

    PageResponse<HealthPackageResponseDTO> getAllHealthPackagesIncludingDeleted(int page, int size);

    PageResponse<HealthPackageShortResponseDTO> searchHealthPackages(String keyword, int page, int size);

    HealthPackageAvailabilityResponseDTO getHealthPackageAvailability(UUID healthPackageId);

    HealthPackageResponseDTO updateHealthPackage(UUID id, UpdateHealthPackageRequestDTO dto);

    void deleteHealthPackage(UUID id);

    HealthPackageResponseDTO restoreHealthPackage(UUID id);
}