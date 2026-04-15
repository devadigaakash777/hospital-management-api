package com.healthcare.hospitalmanagementapi.unit.doctor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.CreateDoctorRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.DoctorResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.DoctorShortResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.UpdateDoctorRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorService doctorService;

    private UUID doctorId;
    private DoctorResponseDTO doctorResponse;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();

        doctorResponse = DoctorResponseDTO.builder()
                .id(doctorId)
                .qualification("MBBS")
                .designation("Consultant")
                .specialization("Cardiology")
                .build();
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldCreateDoctor_whenValidRequest() throws Exception {
        CreateDoctorRequestDTO request = CreateDoctorRequestDTO.builder()
                .userId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .qualification("MBBS")
                .designation("Consultant")
                .specialization("Cardiology")
                .advanceBookingDays(30)
                .build();

        when(doctorService.createDoctor(any(CreateDoctorRequestDTO.class))).thenReturn(doctorResponse);

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/doctors/" + doctorId))
                .andExpect(jsonPath("$.id").value(doctorId.toString()))
                .andExpect(jsonPath("$.qualification").value("MBBS"));
    }

    @Test
    void shouldReturnUnauthorized_whenCreateDoctorWithoutAuthentication() throws Exception {
        CreateDoctorRequestDTO request = CreateDoctorRequestDTO.builder().build();

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenCreateDoctorWithoutRequiredAuthority() throws Exception {
        CreateDoctorRequestDTO request = CreateDoctorRequestDTO.builder()
                .userId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .qualification("MBBS")
                .designation("Consultant")
                .specialization("Cardiology")
                .advanceBookingDays(30)
                .build();

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnBadRequest_whenCreateDoctorWithInvalidRequest() throws Exception {
        CreateDoctorRequestDTO request = CreateDoctorRequestDTO.builder()
                .qualification("")
                .advanceBookingDays(0)
                .build();

        mockMvc.perform(post("/api/v1/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.userId").value("User ID is required"))
                .andExpect(jsonPath("$.errors.departmentId").value("Department ID is required"))
                .andExpect(jsonPath("$.errors.qualification").value("Qualification is required"))
                .andExpect(jsonPath("$.errors.designation").value("Designation is required"))
                .andExpect(jsonPath("$.errors.specialization").value("Specialization is required"))
                .andExpect(jsonPath("$.errors.advanceBookingDays")
                        .value("Advance booking days must be at least 1"));
    }

    @Test
    @WithMockUser
    void shouldGetDoctorById_whenDoctorExists() throws Exception {
        when(doctorService.getDoctorById(doctorId)).thenReturn(doctorResponse);

        mockMvc.perform(get("/api/v1/doctors/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId.toString()))
                .andExpect(jsonPath("$.specialization").value("Cardiology"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenDoctorDoesNotExist() throws Exception {
        when(doctorService.getDoctorById(doctorId))
                .thenThrow(new ResourceNotFoundException("Doctor not found"));

        mockMvc.perform(get("/api/v1/doctors/{doctorId}", doctorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Doctor not found"));
    }

    @Test
    @WithMockUser
    void shouldGetAllDoctors_whenValidPaginationRequest() throws Exception {
        PageResponse<DoctorResponseDTO> response = new PageResponse<>(
                new PageImpl<>(
                        List.of(doctorResponse),
                        PageRequest.of(0, 10),
                        1
                )
        );

        when(doctorService.getAllDoctors(0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(doctorId.toString()))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser
    void shouldSearchDoctors_whenKeywordExists() throws Exception {
        PageResponse<DoctorResponseDTO> response = new PageResponse<>(
                new PageImpl<>(List.of(doctorResponse), PageRequest.of(0, 10), 1)
        );

        when(doctorService.searchDoctors("john", 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/search")
                        .param("keyword", "john")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(doctorId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenSearchDoctorsWithoutKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/doctors/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldGetDoctorsByDepartment_whenDepartmentExists() throws Exception {
        UUID departmentId = UUID.randomUUID();

        DoctorShortResponseDTO response = DoctorShortResponseDTO.builder()
                .id(doctorId)
                .fullName("John Doe")
                .build();

        when(doctorService.getDoctorsByDepartment(departmentId))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/doctors/department/{departmentId}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(doctorId.toString()))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldPatchDoctor_whenValidRequest() throws Exception {
        UpdateDoctorRequestDTO request = UpdateDoctorRequestDTO.builder()
                .qualification("MBBS, MD")
                .advanceBookingDays(20)
                .build();

        when(doctorService.updateDoctor(eq(doctorId), any(UpdateDoctorRequestDTO.class)))
                .thenReturn(doctorResponse);

        mockMvc.perform(patch("/api/v1/doctors/{doctorId}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId.toString()));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnBadRequest_whenPatchDoctorWithInvalidRequest() throws Exception {
        UpdateDoctorRequestDTO request = UpdateDoctorRequestDTO.builder()
                .advanceBookingDays(366)
                .build();

        mockMvc.perform(patch("/api/v1/doctors/{doctorId}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.advanceBookingDays")
                        .value("Advance booking days must not exceed 365"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnNotFound_whenPatchDoctorAndDoctorDoesNotExist() throws Exception {
        UpdateDoctorRequestDTO request = UpdateDoctorRequestDTO.builder()
                .qualification("Updated")
                .build();

        when(doctorService.updateDoctor(eq(doctorId), any(UpdateDoctorRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Doctor not found"));

        mockMvc.perform(patch("/api/v1/doctors/{doctorId}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_DOCTOR_SLOTS"})
    void shouldDeleteDoctor_whenAuthorized() throws Exception {
        doNothing().when(doctorService).deleteDoctor(doctorId);

        mockMvc.perform(delete("/api/v1/doctors/{doctorId}", doctorId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnForbidden_whenDeleteDoctorWithoutAdminRole() throws Exception {
        mockMvc.perform(delete("/api/v1/doctors/{doctorId}", doctorId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_DOCTOR_SLOTS"})
    void shouldRestoreDoctor_whenDoctorIsDeleted() throws Exception {
        when(doctorService.restoreDoctor(doctorId)).thenReturn(doctorResponse);

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/restore", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId.toString()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_DOCTOR_SLOTS"})
    void shouldReturnConflict_whenRestoreDoctorAlreadyActive() throws Exception {
        when(doctorService.restoreDoctor(doctorId))
                .thenThrow(new ConflictException("Doctor is already active and cannot be restored"));

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/restore", doctorId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("Doctor is already active and cannot be restored"));
    }
}