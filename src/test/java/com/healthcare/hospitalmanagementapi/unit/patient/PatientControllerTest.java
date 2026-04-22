package com.healthcare.hospitalmanagementapi.unit.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.patient.dto.CreatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.PatientResponseDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.UpdatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientService patientService;

    private UUID patientId;
    private PatientResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();

        responseDTO = PatientResponseDTO.builder()
                .id(patientId)
                .firstName("Akash")
                .lastName("Devadiga")
                .uhId("UH-2026-0001")
                .phoneNumber("+919876543210")
                .email("akash@example.com")
                .build();
    }

    @Test
    @WithMockUser
    void shouldCreatePatient_whenValidRequest() throws Exception {
        CreatePatientRequestDTO request = CreatePatientRequestDTO.builder()
                .firstName("Akash")
                .lastName("Devadiga")
                .phoneNumber("+919876543210")
                .email("akash@example.com")
                .build();

        when(patientService.createPatient(any(CreatePatientRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/patients/" + patientId))
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.uhId").value("UH-2026-0001"));
    }

    @Test
    void shouldReturnUnauthorized_whenCreatePatientWithoutAuthentication() throws Exception {
        CreatePatientRequestDTO request = CreatePatientRequestDTO.builder().build();

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreatePatientWithInvalidRequest() throws Exception {
        CreatePatientRequestDTO request = CreatePatientRequestDTO.builder()
                .firstName("")
                .lastName("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @WithMockUser
    void shouldGetPatientById_whenPatientExists() throws Exception {
        when(patientService.getPatientById(patientId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/patients/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("Akash"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenGetPatientByIdAndPatientDoesNotExist() throws Exception {
        when(patientService.getPatientById(patientId))
                .thenThrow(new ResourceNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/v1/patients/{patientId}", patientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Patient not found"));
    }

    @Test
    @WithMockUser
    void shouldGetAllPatients_whenValidPaginationRequest() throws Exception {
        PageResponse<PatientResponseDTO> response = new PageResponse<>(
                new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 10), 1)
        );

        when(patientService.getAllPatients(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/patients")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(patientId.toString()))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser
    void shouldSearchPatients_whenSearchKeywordProvided() throws Exception {
        PageResponse<PatientResponseDTO> response = new PageResponse<>(
                new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 10), 1)
        );

        when(patientService.searchPatients(eq("akash"), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/patients/search")
                        .param("search", "akash")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(patientId.toString()));
    }

    @Test
    @WithMockUser
    void shouldUpdatePatient_whenValidRequest() throws Exception {
        UpdatePatientRequestDTO request = UpdatePatientRequestDTO.builder()
                .firstName("Updated")
                .build();

        when(patientService.updatePatient(eq(patientId), any(UpdatePatientRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/patients/{patientId}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenUpdatePatientWithInvalidEmail() throws Exception {
        UpdatePatientRequestDTO request = UpdatePatientRequestDTO.builder()
                .email("invalid-email")
                .build();

        mockMvc.perform(patch("/api/v1/patients/{patientId}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @WithMockUser
    void shouldDeletePatient_whenPatientExists() throws Exception {
        doNothing().when(patientService).deletePatient(patientId);

        mockMvc.perform(delete("/api/v1/patients/{patientId}", patientId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldRestorePatient_whenPatientIsDeleted() throws Exception {
        when(patientService.restorePatient(patientId)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/patients/{patientId}/restore", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenRestorePatientAlreadyActive() throws Exception {
        when(patientService.restorePatient(patientId))
                .thenThrow(new ConflictException("Patient is already active"));

        mockMvc.perform(post("/api/v1/patients/{patientId}/restore", patientId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Patient is already active"));
    }
}