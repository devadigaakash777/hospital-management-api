package com.healthcare.hospitalmanagementapi.unit.patient;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.patient.dto.CreatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.PatientResponseDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.UpdatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import com.healthcare.hospitalmanagementapi.patient.mapper.PatientMapper;
import com.healthcare.hospitalmanagementapi.patient.repository.PatientRepository;
import com.healthcare.hospitalmanagementapi.patient.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    private UUID patientId;
    private Patient patient;
    private PatientResponseDTO responseDTO;
    private CreatePatientRequestDTO createRequest;
    private UpdatePatientRequestDTO updateRequest;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();

        patient = Patient.builder()
                .id(patientId)
                .firstName("Akash")
                .lastName("Devadiga")
                .uhId("UH-" + Year.now().getValue() + "-0001")
                .phoneNumber("+919876543210")
                .email("akash@example.com")
                .isDeleted(false)
                .build();

        responseDTO = PatientResponseDTO.builder()
                .id(patientId)
                .firstName("Akash")
                .lastName("Devadiga")
                .uhId(patient.getUhId())
                .phoneNumber(patient.getPhoneNumber())
                .email(patient.getEmail())
                .build();

        createRequest = CreatePatientRequestDTO.builder()
                .firstName("Akash")
                .lastName("Devadiga")
                .phoneNumber("+919876543210")
                .email("akash@example.com")
                .build();

        updateRequest = UpdatePatientRequestDTO.builder()
                .firstName("Updated")
                .phoneNumber("+911111111111")
                .build();
    }

    @Test
    void shouldCreatePatient_whenValidInput() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "UH-" + year + "-";

        when(patientMapper.toEntity(createRequest)).thenReturn(patient);
        when(patientRepository.findLatestUhIdByPrefix(prefix)).thenReturn(Optional.of(prefix + "0007"));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

        PatientResponseDTO result = patientService.createPatient(createRequest);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(patientCaptor.capture());

        Patient savedPatient = patientCaptor.getValue();
        assertThat(savedPatient.getUhId()).isEqualTo(prefix + "0008");

        verify(patientMapper).toEntity(createRequest);
        verify(patientMapper).toResponseDTO(patient);
    }

    @Test
    void shouldCreatePatientWithFirstUhId_whenNoPreviousUhIdExists() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "UH-" + year + "-";

        when(patientMapper.toEntity(createRequest)).thenReturn(patient);
        when(patientRepository.findLatestUhIdByPrefix(prefix)).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

        patientService.createPatient(createRequest);

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(patientCaptor.capture());

        assertThat(patientCaptor.getValue().getUhId()).isEqualTo(prefix + "0001");
    }

    @Test
    void shouldReturnPatient_whenPatientExists() {
        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.of(patient));
        when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

        PatientResponseDTO result = patientService.getPatientById(patientId);

        assertThat(result).isEqualTo(responseDTO);

        verify(patientRepository).findByIdAndIsDeletedFalse(patientId);
        verify(patientMapper).toResponseDTO(patient);
    }

    @Test
    void shouldThrowException_whenPatientDoesNotExist() {
        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(patientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found with id: " + patientId);
    }

    @Test
    void shouldReturnPagedPatients_whenGetAllPatients() {
        Page<Patient> patientPage = new PageImpl<>(
                List.of(patient),
                PageRequest.of(0, 10),
                1
        );

        when(patientRepository.findAllByIsDeletedFalse(any())).thenReturn(patientPage);
        when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

        PageResponse<PatientResponseDTO> result =
                patientService.getAllPatients(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(responseDTO);
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldSearchPatients_whenSearchKeywordProvided() {
        Page<Patient> patientPage = new PageImpl<>(List.of(patient));

        when(patientRepository.search(eq("akash"), any())).thenReturn(patientPage);
        when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

        PageResponse<PatientResponseDTO> result =
                patientService.searchPatients("akash", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getFirstName()).isEqualTo("Akash");
    }

    @Test
    void shouldUpdatePatient_whenValidRequest() {
        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

        PatientResponseDTO result = patientService.updatePatient(patientId, updateRequest);

        assertThat(result).isEqualTo(responseDTO);

        verify(patientMapper).updateEntity(updateRequest, patient);
        verify(patientRepository).save(patient);
        verify(patientMapper).toResponseDTO(patient);
    }

    @Test
    void shouldThrowException_whenUpdatePatientAndPatientNotFound() {
        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(patientId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found with id: " + patientId);

        verify(patientRepository, never()).save(any());
    }

    @Test
    void shouldDeletePatient_whenPatientExists() {
        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.of(patient));

        patientService.deletePatient(patientId);

        verify(patientRepository).delete(patient);
    }

    @Test
    void shouldThrowException_whenDeletePatientAndPatientNotFound() {
        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.deletePatient(patientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found with id: " + patientId);

        verify(patientRepository, never()).delete(any());
    }

    @Test
    void shouldRestorePatient_whenPatientIsDeleted() {
        patient.setIsDeleted(true);
        patient.setDeletedAt(LocalDateTime.now());

        when(patientRepository.findByIdIncludingDeleted(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponseDTO(patient)).thenReturn(responseDTO);

        PatientResponseDTO result = patientService.restorePatient(patientId);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(patient.getIsDeleted()).isFalse();
        assertThat(patient.getDeletedAt()).isNull();

        verify(patientRepository).save(patient);
    }

    @Test
    void shouldThrowConflictException_whenRestorePatientAndPatientAlreadyActive() {
        patient.setIsDeleted(false);

        when(patientRepository.findByIdIncludingDeleted(patientId)).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.restorePatient(patientId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Patient is already active");
    }

    @Test
    void shouldThrowException_whenRestorePatientAndPatientNotFound() {
        when(patientRepository.findByIdIncludingDeleted(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.restorePatient(patientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found with id: " + patientId);
    }
}