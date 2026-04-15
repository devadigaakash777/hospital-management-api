package com.healthcare.hospitalmanagementapi.unit.weeklyschedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.CreateDoctorWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.DoctorWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorWeeklyScheduleService;
import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.doThrow;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DoctorWeeklyScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorWeeklyScheduleService doctorWeeklyScheduleService;

    private UUID doctorId;
    private UUID scheduleId;

    private DoctorWeeklyScheduleResponseDTO responseDTO;
    private CreateDoctorWeeklyScheduleRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        validRequest = CreateDoctorWeeklyScheduleRequestDTO.builder()
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .build();

        responseDTO = DoctorWeeklyScheduleResponseDTO.builder()
                .id(scheduleId)
                .doctorId(doctorId)
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .build();
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldCreateWeeklySchedules_whenValidRequest() throws Exception {
        when(doctorWeeklyScheduleService.bulkCreate(eq(doctorId), anyList()))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(validRequest))))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/doctors/" + doctorId + "/weekly-schedules"
                ))
                .andExpect(jsonPath("$[0].id").value(scheduleId.toString()))
                .andExpect(jsonPath("$[0].weekNumber").value(1))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    void shouldReturnUnauthorized_whenCreateWeeklySchedulesWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(validRequest))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenCreateWeeklySchedulesWithoutRequiredAuthority() throws Exception {
        mockMvc.perform(post("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(validRequest))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnBadRequest_whenCreateWeeklySchedulesWithInvalidRequest() throws Exception {
        CreateDoctorWeeklyScheduleRequestDTO invalidRequest =
                CreateDoctorWeeklyScheduleRequestDTO.builder()
                        .weekNumber(6)
                        .build();

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(invalidRequest))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Validation failure"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnConflict_whenCreateWeeklySchedulesAndDuplicateExists() throws Exception {
        when(doctorWeeklyScheduleService.bulkCreate(eq(doctorId), anyList()))
                .thenThrow(new ConflictException(
                        "Schedule already exists for weekNumber 1 and day MONDAY"
                ));

        mockMvc.perform(post("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(validRequest))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(
                        "Schedule already exists for weekNumber 1 and day MONDAY"
                ));
    }

    @Test
    @WithMockUser
    void shouldGetAllWeeklySchedules_whenDoctorExists() throws Exception {
        when(doctorWeeklyScheduleService.getAllByDoctor(doctorId))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(scheduleId.toString()))
                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenGetAllWeeklySchedulesAndDoctorDoesNotExist() throws Exception {
        when(doctorWeeklyScheduleService.getAllByDoctor(doctorId))
                .thenThrow(new ResourceNotFoundException(
                        "Doctor not found with id: " + doctorId
                ));

        mockMvc.perform(get("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(
                        "Doctor not found with id: " + doctorId
                ));
    }

    @Test
    @WithMockUser
    void shouldGetWeeklyScheduleById_whenScheduleExists() throws Exception {
        when(doctorWeeklyScheduleService.getById(doctorId, scheduleId))
                .thenReturn(responseDTO);

        mockMvc.perform(get(
                        "/api/v1/doctors/{doctorId}/weekly-schedules/{scheduleId}",
                        doctorId,
                        scheduleId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.weekNumber").value(1));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenGetWeeklyScheduleByIdAndScheduleDoesNotExist() throws Exception {
        when(doctorWeeklyScheduleService.getById(doctorId, scheduleId))
                .thenThrow(new ResourceNotFoundException(
                        "Schedule not found with id: " + scheduleId
                ));

        mockMvc.perform(get(
                        "/api/v1/doctors/{doctorId}/weekly-schedules/{scheduleId}",
                        doctorId,
                        scheduleId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(
                        "Schedule not found with id: " + scheduleId
                ));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldUpdateWeeklySchedules_whenValidRequest() throws Exception {
        when(doctorWeeklyScheduleService.bulkUpdate(eq(doctorId), anyList()))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(put("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(validRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(scheduleId.toString()))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnConflict_whenUpdateWeeklySchedulesContainsDuplicate() throws Exception {
        when(doctorWeeklyScheduleService.bulkUpdate(eq(doctorId), anyList()))
                .thenThrow(new ConflictException(
                        "Duplicate schedule found for weekNumber 1 and day MONDAY"
                ));

        mockMvc.perform(put("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(validRequest))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(
                        "Duplicate schedule found for weekNumber 1 and day MONDAY"
                ));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldDeleteWeeklySchedules_whenAuthorized() throws Exception {
        doNothing().when(doctorWeeklyScheduleService)
                .bulkDelete(eq(doctorId), anyList());

        mockMvc.perform(delete("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .param("scheduleIds", scheduleId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldReturnForbidden_whenDeleteWeeklySchedulesWithoutRequiredAuthority() throws Exception {
        mockMvc.perform(delete("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .param("scheduleIds", scheduleId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_DOCTOR_SLOTS")
    void shouldReturnNotFound_whenDeleteWeeklySchedulesAndScheduleDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Schedule not found with id: " + scheduleId
        )).when(doctorWeeklyScheduleService)
                .bulkDelete(eq(doctorId), anyList());

        mockMvc.perform(delete("/api/v1/doctors/{doctorId}/weekly-schedules", doctorId)
                        .param("scheduleIds", scheduleId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(
                        "Schedule not found with id: " + scheduleId
                ));
    }
}