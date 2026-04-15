package com.healthcare.hospitalmanagementapi.unit.blockdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.auth.security.DoctorSecurity;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.CreateDoctorBlockedDateRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.DoctorBlockedDateResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorBlockedDateService;

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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DoctorBlockedDateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorBlockedDateService blockedDateService;

    @MockitoBean
    private DoctorSecurity doctorSecurity;

    private UUID doctorId;
    private UUID blockedDateId;
    private DoctorBlockedDateResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        blockedDateId = UUID.randomUUID();

        when(doctorSecurity.isSelfOrAdmin(any(UUID.class))).thenReturn(true);

        responseDTO = DoctorBlockedDateResponseDTO.builder()
                .id(blockedDateId)
                .doctorId(doctorId)
                .blockedDate(LocalDate.now().plusDays(1))
                .blockReason("Conference")
                .build();
    }

    @Test
    @WithMockUser
    void shouldCreateBlockedDate_whenValidRequest() throws Exception {
        CreateDoctorBlockedDateRequestDTO request =
                CreateDoctorBlockedDateRequestDTO.builder()
                        .blockedDate(LocalDate.now().plusDays(1))
                        .blockReason("Conference")
                        .build();

        when(blockedDateService.createBlockedDate(eq(doctorId), any()))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/blocked-dates", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/doctors/" + doctorId + "/blocked-dates/" + blockedDateId))
                .andExpect(jsonPath("$.id").value(blockedDateId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()));
    }

    @Test
    void shouldReturnUnauthorized_whenCreateBlockedDateWithoutAuthentication() throws Exception {
        CreateDoctorBlockedDateRequestDTO request =
                CreateDoctorBlockedDateRequestDTO.builder()
                        .blockedDate(LocalDate.now().plusDays(1))
                        .build();

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/blocked-dates", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreateBlockedDateWithInvalidRequest() throws Exception {
        CreateDoctorBlockedDateRequestDTO request =
                CreateDoctorBlockedDateRequestDTO.builder()
                        .blockReason("a".repeat(1001))
                        .build();

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/blocked-dates", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.blockedDate").value("Blocked date is required"))
                .andExpect(jsonPath("$.errors.blockReason")
                        .value("Block reason must not exceed 1000 characters"));
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenBlockedDateAlreadyExists() throws Exception {
        CreateDoctorBlockedDateRequestDTO request =
                CreateDoctorBlockedDateRequestDTO.builder()
                        .blockedDate(LocalDate.now().plusDays(1))
                        .blockReason("Conference")
                        .build();

        when(blockedDateService.createBlockedDate(eq(doctorId), any()))
                .thenThrow(new ConflictException(
                        "Doctor already has a blocked date on " + request.getBlockedDate()
                ));

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/blocked-dates", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("Doctor already has a blocked date on " + request.getBlockedDate()));
    }

    @Test
    @WithMockUser
    void shouldGetBlockedDateById_whenBlockedDateExists() throws Exception {
        when(blockedDateService.getBlockedDateById(blockedDateId))
                .thenReturn(responseDTO);

        mockMvc.perform(get(
                        "/api/v1/doctors/{doctorId}/blocked-dates/{blockedDateId}",
                        doctorId,
                        blockedDateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(blockedDateId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenBlockedDateDoesNotExist() throws Exception {
        when(blockedDateService.getBlockedDateById(blockedDateId))
                .thenThrow(new ResourceNotFoundException("Blocked date not found"));

        mockMvc.perform(get(
                        "/api/v1/doctors/{doctorId}/blocked-dates/{blockedDateId}",
                        doctorId,
                        blockedDateId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Blocked date not found"));
    }

    @Test
    @WithMockUser
    void shouldGetBlockedDatesByDoctor_whenPaginationProvided() throws Exception {
        PageResponse<DoctorBlockedDateResponseDTO> response =
                new PageResponse<>(
                        new PageImpl<>(
                                List.of(responseDTO),
                                PageRequest.of(0, 10),
                                1
                        )
                );

        when(blockedDateService.getBlockedDatesByDoctor(eq(doctorId), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/blocked-dates", doctorId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(blockedDateId.toString()))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser
    void shouldGetBlockedDatesByDateRange_whenValidDateRange() throws Exception {
        PageResponse<DoctorBlockedDateResponseDTO> response =
                new PageResponse<>(
                        new PageImpl<>(
                                List.of(responseDTO),
                                PageRequest.of(0, 10),
                                1
                        )
                );

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        when(blockedDateService.getBlockedDatesByDateRange(
                eq(doctorId),
                eq(startDate),
                eq(endDate),
                any()
        )).thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/blocked-dates/range", doctorId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(blockedDateId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenStartDateIsAfterEndDate() throws Exception {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now();

        when(blockedDateService.getBlockedDatesByDateRange(
                eq(doctorId),
                eq(startDate),
                eq(endDate),
                any()
        )).thenThrow(new ConflictException("Start date cannot be after end date"));

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/blocked-dates/range", doctorId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("Start date cannot be after end date"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_DOCTOR_SLOTS"})
    void shouldDeleteBlockedDate_whenAuthorized() throws Exception {
        doNothing().when(blockedDateService).deleteBlockedDate(blockedDateId);

        mockMvc.perform(delete(
                        "/api/v1/doctors/{doctorId}/blocked-dates/{blockedDateId}",
                        doctorId,
                        blockedDateId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenDeleteBlockedDateWithoutRequiredAuthority() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/doctors/{doctorId}/blocked-dates/{blockedDateId}",
                        doctorId,
                        blockedDateId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_DOCTOR_SLOTS"})
    void shouldReturnConflict_whenDeleteBlockedDateAfterDoctorSlotStarted() throws Exception {
        doThrow(new ConflictException(
                "Blocked date can only be deleted before the first doctor time slot starts"
        )).when(blockedDateService).deleteBlockedDate(blockedDateId);

        mockMvc.perform(delete(
                        "/api/v1/doctors/{doctorId}/blocked-dates/{blockedDateId}",
                        doctorId,
                        blockedDateId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("Blocked date can only be deleted before the first doctor time slot starts"));
    }
}