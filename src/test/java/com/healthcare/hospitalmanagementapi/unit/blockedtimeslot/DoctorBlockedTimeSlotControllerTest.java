package com.healthcare.hospitalmanagementapi.unit.blockedtimeslot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.auth.security.DoctorSecurity;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.CreateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.DoctorBlockedTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.UpdateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorBlockedTimeSlotService;
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
import java.time.LocalTime;
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
class DoctorBlockedTimeSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorBlockedTimeSlotService blockedTimeSlotService;

    @MockitoBean
    private DoctorSecurity doctorSecurity;

    private UUID doctorId;
    private UUID blockedTimeSlotId;
    private DoctorBlockedTimeSlotResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        blockedTimeSlotId = UUID.randomUUID();

        when(doctorSecurity.isSelfOrAdmin(any(UUID.class))).thenReturn(true);

        responseDTO = DoctorBlockedTimeSlotResponseDTO.builder()
                .id(blockedTimeSlotId)
                .doctorId(doctorId)
                .blockedDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .reservedSlots(2)
                .blockReason("Conference")
                .build();
    }

    @Test
    @WithMockUser
    void shouldCreateBlockedTimeSlot_whenValidRequest() throws Exception {
        CreateDoctorBlockedTimeSlotRequestDTO request =
                CreateDoctorBlockedTimeSlotRequestDTO.builder()
                        .blockedDate(LocalDate.now().plusDays(1))
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(12, 0))
                        .reservedSlots(2)
                        .blockReason("Conference")
                        .build();

        when(blockedTimeSlotService.createBlockedTimeSlot(eq(doctorId), any()))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/blocked-time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/doctors/" + doctorId + "/blocked-time-slots/" + blockedTimeSlotId))
                .andExpect(jsonPath("$.id").value(blockedTimeSlotId.toString()))
                .andExpect(jsonPath("$.blockReason").value("Conference"));
    }

    @Test
    void shouldReturnUnauthorized_whenCreateBlockedTimeSlotWithoutAuthentication() throws Exception {
        CreateDoctorBlockedTimeSlotRequestDTO request =
                CreateDoctorBlockedTimeSlotRequestDTO.builder().build();

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/blocked-time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreateBlockedTimeSlotWithInvalidRequest() throws Exception {
        CreateDoctorBlockedTimeSlotRequestDTO request =
                CreateDoctorBlockedTimeSlotRequestDTO.builder()
                        .reservedSlots(-1)
                        .blockReason("a".repeat(1001))
                        .build();

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/blocked-time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.blockedDate").value("Blocked date is required"))
                .andExpect(jsonPath("$.errors.startTime").value("Start time is required"))
                .andExpect(jsonPath("$.errors.endTime").value("End time is required"))
                .andExpect(jsonPath("$.errors.reservedSlots")
                        .value("Reserved slots cannot be negative"));
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenCreateBlockedTimeSlotOverlaps() throws Exception {
        CreateDoctorBlockedTimeSlotRequestDTO request =
                CreateDoctorBlockedTimeSlotRequestDTO.builder()
                        .blockedDate(LocalDate.now().plusDays(1))
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(12, 0))
                        .build();

        when(blockedTimeSlotService.createBlockedTimeSlot(eq(doctorId), any()))
                .thenThrow(new ConflictException(
                        "Blocked time slot overlaps with an existing blocked time slot"));

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/blocked-time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @WithMockUser
    void shouldGetBlockedTimeSlotById_whenExists() throws Exception {
        when(blockedTimeSlotService.getBlockedTimeSlotById(blockedTimeSlotId))
                .thenReturn(responseDTO);

        mockMvc.perform(get(
                        "/api/v1/doctors/{doctorId}/blocked-time-slots/{blockedTimeSlotId}",
                        doctorId,
                        blockedTimeSlotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(blockedTimeSlotId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenBlockedTimeSlotDoesNotExist() throws Exception {
        when(blockedTimeSlotService.getBlockedTimeSlotById(blockedTimeSlotId))
                .thenThrow(new ResourceNotFoundException("Blocked time slot not found"));

        mockMvc.perform(get(
                        "/api/v1/doctors/{doctorId}/blocked-time-slots/{blockedTimeSlotId}",
                        doctorId,
                        blockedTimeSlotId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Blocked time slot not found"));
    }

    @Test
    @WithMockUser
    void shouldGetBlockedTimeSlotsByDoctor_whenPaginationProvided() throws Exception {
        PageResponse<DoctorBlockedTimeSlotResponseDTO> response =
                new PageResponse<>(
                        new PageImpl<>(
                                List.of(responseDTO),
                                PageRequest.of(0, 10),
                                1
                        )
                );

        when(blockedTimeSlotService.getBlockedTimeSlotsByDoctor(eq(doctorId), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/blocked-time-slots", doctorId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(blockedTimeSlotId.toString()))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser
    void shouldGetBlockedTimeSlotsByDate_whenDateProvided() throws Exception {
        PageResponse<DoctorBlockedTimeSlotResponseDTO> response =
                new PageResponse<>(
                        new PageImpl<>(
                                List.of(responseDTO),
                                PageRequest.of(0, 10),
                                1
                        )
                );

        when(blockedTimeSlotService.getBlockedTimeSlotsByDate(
                eq(doctorId),
                eq(responseDTO.getBlockedDate()),
                any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/blocked-time-slots/date", doctorId)
                        .param("blockedDate", responseDTO.getBlockedDate().toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].blockedDate")
                        .value(responseDTO.getBlockedDate().toString()));
    }

    @Test
    @WithMockUser
    void shouldPatchBlockedTimeSlot_whenValidRequest() throws Exception {
        UpdateDoctorBlockedTimeSlotRequestDTO request =
                UpdateDoctorBlockedTimeSlotRequestDTO.builder()
                        .endTime(LocalTime.of(13, 0))
                        .blockReason("Updated")
                        .build();

        when(blockedTimeSlotService.updateBlockedTimeSlot(eq(blockedTimeSlotId), any()))
                .thenReturn(responseDTO);

        mockMvc.perform(patch(
                        "/api/v1/doctors/{doctorId}/blocked-time-slots/{blockedTimeSlotId}",
                        doctorId,
                        blockedTimeSlotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(blockedTimeSlotId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenPatchBlockedTimeSlotWithInvalidRequest() throws Exception {
        UpdateDoctorBlockedTimeSlotRequestDTO request =
                UpdateDoctorBlockedTimeSlotRequestDTO.builder()
                        .reservedSlots(-1)
                        .blockReason("a".repeat(1001))
                        .build();

        mockMvc.perform(patch(
                        "/api/v1/doctors/{doctorId}/blocked-time-slots/{blockedTimeSlotId}",
                        doctorId,
                        blockedTimeSlotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.reservedSlots")
                        .value("Reserved slots cannot be negative"));
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenPatchBlockedTimeSlotViolatesBusinessRule() throws Exception {
        UpdateDoctorBlockedTimeSlotRequestDTO request =
                UpdateDoctorBlockedTimeSlotRequestDTO.builder()
                        .endTime(LocalTime.of(9, 0))
                        .build();

        when(blockedTimeSlotService.updateBlockedTimeSlot(eq(blockedTimeSlotId), any()))
                .thenThrow(new ConflictException("End time must be greater than current time"));

        mockMvc.perform(patch(
                        "/api/v1/doctors/{doctorId}/blocked-time-slots/{blockedTimeSlotId}",
                        doctorId,
                        blockedTimeSlotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("End time must be greater than current time"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_DOCTOR_SLOTS"})
    void shouldDeleteBlockedTimeSlot_whenAuthorized() throws Exception {
        doNothing().when(blockedTimeSlotService).deleteBlockedTimeSlot(blockedTimeSlotId);

        mockMvc.perform(delete(
                        "/api/v1/doctors/{doctorId}/blocked-time-slots/{blockedTimeSlotId}",
                        doctorId,
                        blockedTimeSlotId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenDeleteBlockedTimeSlotWithoutRequiredAuthority() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/doctors/{doctorId}/blocked-time-slots/{blockedTimeSlotId}",
                        doctorId,
                        blockedTimeSlotId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }
}