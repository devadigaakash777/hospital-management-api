package com.healthcare.hospitalmanagementapi.unit.timeslot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.CreateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.DoctorTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.UpdateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorTimeSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DoctorTimeSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorTimeSlotService doctorTimeSlotService;

    private UUID doctorId;
    private UUID slotId;
    private DoctorTimeSlotResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        slotId = UUID.randomUUID();

        responseDTO = DoctorTimeSlotResponseDTO.builder()
                .id(slotId)
                .doctorId(doctorId)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .totalSlots(10)
                .reservedSlots(3)
                .patientsPerSlot(7)
                .build();
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldCreateTimeSlot_whenValidRequest() throws Exception {
        CreateDoctorTimeSlotRequestDTO request = CreateDoctorTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .totalSlots(10)
                .reservedSlots(3)
                .build();

        when(doctorTimeSlotService.create(eq(doctorId), any(CreateDoctorTimeSlotRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/doctors/" + doctorId + "/time-slots/" + slotId
                ))
                .andExpect(jsonPath("$.id").value(slotId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.patientsPerSlot").value(7));
    }

    @Test
    void shouldReturnUnauthorized_whenCreateTimeSlotWithoutAuthentication() throws Exception {
        CreateDoctorTimeSlotRequestDTO request = CreateDoctorTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .totalSlots(10)
                .reservedSlots(3)
                .build();

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenCreateTimeSlotWithoutRequiredAuthority() throws Exception {
        CreateDoctorTimeSlotRequestDTO request = CreateDoctorTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .totalSlots(10)
                .reservedSlots(3)
                .build();

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnBadRequest_whenCreateTimeSlotWithInvalidRequest() throws Exception {
        CreateDoctorTimeSlotRequestDTO request = CreateDoctorTimeSlotRequestDTO.builder()
                .startTime(null)
                .endTime(null)
                .totalSlots(0)
                .reservedSlots(-1)
                .build();

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.startTime").value("Start time is required"))
                .andExpect(jsonPath("$.errors.endTime").value("End time is required"))
                .andExpect(jsonPath("$.errors.totalSlots").value("Total slots must be at least 1"))
                .andExpect(jsonPath("$.errors.reservedSlots").value("Reserved slots cannot be negative"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnConflict_whenCreateTimeSlotOverlapsExistingSlot() throws Exception {
        CreateDoctorTimeSlotRequestDTO request = CreateDoctorTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .totalSlots(10)
                .reservedSlots(3)
                .build();

        when(doctorTimeSlotService.create(eq(doctorId), any(CreateDoctorTimeSlotRequestDTO.class)))
                .thenThrow(new ConflictException("Time slot overlaps with an existing time slot"));

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/time-slots", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("Time slot overlaps with an existing time slot"));
    }

    @Test
    @WithMockUser
    void shouldGetAllTimeSlots_whenDoctorExists() throws Exception {
        when(doctorTimeSlotService.getAllByDoctor(doctorId))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/time-slots", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(slotId.toString()))
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenGetAllTimeSlotsAndDoctorDoesNotExist() throws Exception {
        when(doctorTimeSlotService.getAllByDoctor(doctorId))
                .thenThrow(new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/time-slots", doctorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldUpdateTimeSlot_whenValidRequest() throws Exception {
        UpdateDoctorTimeSlotRequestDTO request = UpdateDoctorTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .totalSlots(12)
                .reservedSlots(4)
                .build();

        when(doctorTimeSlotService.update(eq(doctorId), eq(slotId), any(UpdateDoctorTimeSlotRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/doctors/{doctorId}/time-slots/{slotId}", doctorId, slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(slotId.toString()));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnBadRequest_whenUpdateTimeSlotWithInvalidReservedSlots() throws Exception {
        UpdateDoctorTimeSlotRequestDTO request = UpdateDoctorTimeSlotRequestDTO.builder()
                .reservedSlots(-1)
                .build();

        mockMvc.perform(patch("/api/v1/doctors/{doctorId}/time-slots/{slotId}", doctorId, slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.reservedSlots")
                        .value("Reserved slots cannot be negative"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnNotFound_whenUpdateTimeSlotAndSlotDoesNotExist() throws Exception {
        UpdateDoctorTimeSlotRequestDTO request = UpdateDoctorTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .build();

        when(doctorTimeSlotService.update(eq(doctorId), eq(slotId), any(UpdateDoctorTimeSlotRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Time slot not found with id: " + slotId));

        mockMvc.perform(patch("/api/v1/doctors/{doctorId}/time-slots/{slotId}", doctorId, slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Time slot not found with id: " + slotId));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldDeleteTimeSlot_whenAuthorized() throws Exception {
        doNothing().when(doctorTimeSlotService).delete(doctorId, slotId);

        mockMvc.perform(delete("/api/v1/doctors/{doctorId}/time-slots/{slotId}", doctorId, slotId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenDeleteTimeSlotWithoutRequiredAuthority() throws Exception {
        mockMvc.perform(delete("/api/v1/doctors/{doctorId}/time-slots/{slotId}", doctorId, slotId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }
}