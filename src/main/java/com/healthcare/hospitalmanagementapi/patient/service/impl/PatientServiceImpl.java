package com.healthcare.hospitalmanagementapi.patient.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.patient.dto.CreatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.PatientResponseDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.UpdatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import com.healthcare.hospitalmanagementapi.patient.mapper.PatientMapper;
import com.healthcare.hospitalmanagementapi.patient.repository.PatientRepository;
import com.healthcare.hospitalmanagementapi.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "patients")
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private static final String PATIENT_NOT_FOUND_MESSAGE = "Patient not found with id: ";

    @Override
    @CachePut(key = "#result.id")
    public PatientResponseDTO createPatient(CreatePatientRequestDTO requestDTO) {

        Patient patient = patientMapper.toEntity(requestDTO);

        patient.setUhId(generateNextUhId());

        Patient savedPatient = patientRepository.save(patient);

        log.info(
                "Patient created with id: {} and uhId: {}",
                savedPatient.getId(),
                savedPatient.getUhId()
        );

        return patientMapper.toResponseDTO(savedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public PatientResponseDTO getPatientById(UUID id) {
        Patient patient = patientRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PATIENT_NOT_FOUND_MESSAGE + id
                ));

        return patientMapper.toResponseDTO(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> searchPatients(String search, Pageable pageable) {
        Page<Patient> patientPage = patientRepository.search(search, pageable);

        Page<PatientResponseDTO> responsePage = patientPage.map(patientMapper::toResponseDTO);

        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> getAllPatients(Pageable pageable) {
        Page<Patient> patientPage = patientRepository.findAllByIsDeletedFalse(pageable);

        Page<PatientResponseDTO> responsePage = patientPage.map(patientMapper::toResponseDTO);

        return PageResponse.of(responsePage);
    }

    @Override
    @CachePut(key = "#id")
    public PatientResponseDTO updatePatient(UUID id, UpdatePatientRequestDTO requestDTO) {
        Patient patient = patientRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PATIENT_NOT_FOUND_MESSAGE + id
                ));

        patientMapper.updateEntity(requestDTO, patient);

        Patient updatedPatient = patientRepository.save(patient);

        log.info("Patient updated with id: {}", updatedPatient.getId());

        return patientMapper.toResponseDTO(updatedPatient);
    }

    @Override
    @CacheEvict(key = "#id")
    public void deletePatient(UUID id) {
        Patient patient = patientRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PATIENT_NOT_FOUND_MESSAGE + id
                ));

        patientRepository.delete(patient);

        log.info("Patient soft deleted with id: {}", id);
    }

    @Override
    @CachePut(key = "#id")
    public PatientResponseDTO restorePatient(UUID id) {
        Patient patient = patientRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PATIENT_NOT_FOUND_MESSAGE + id
                ));

        if (Boolean.FALSE.equals(patient.getIsDeleted())) {
            throw new ConflictException("Patient is already active");
        }

        patient.setIsDeleted(false);
        patient.setDeletedAt(null);

        Patient restoredPatient = patientRepository.save(patient);

        log.info("Patient restored with id: {}", restoredPatient.getId());

        return patientMapper.toResponseDTO(restoredPatient);
    }

    private synchronized String generateNextUhId() {
        String year = String.valueOf(java.time.Year.now().getValue());
        String prefix = "UH-" + year + "-";

        Optional<String> latestUhIdOptional = patientRepository.findLatestUhIdByPrefix(prefix);

        int nextNumber = latestUhIdOptional
                .map(latestUhId -> {
                    String numberPart = latestUhId.substring(prefix.length());
                    return Integer.parseInt(numberPart) + 1;
                })
                .orElse(1);

        return prefix + String.format("%04d", nextNumber);
    }
}