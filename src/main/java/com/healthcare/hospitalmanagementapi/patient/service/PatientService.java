package com.healthcare.hospitalmanagementapi.patient.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.patient.dto.CreatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.PatientResponseDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.UpdatePatientRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PatientService {

    PatientResponseDTO createPatient(CreatePatientRequestDTO requestDTO);

    PatientResponseDTO getPatientById(UUID id);

    PageResponse<PatientResponseDTO> searchPatients(String search, Pageable pageable);

    PageResponse<PatientResponseDTO> getAllPatients(Pageable pageable);

    PatientResponseDTO updatePatient(UUID id, UpdatePatientRequestDTO requestDTO);

    void deletePatient(UUID id);

    PatientResponseDTO restorePatient(UUID id);
}