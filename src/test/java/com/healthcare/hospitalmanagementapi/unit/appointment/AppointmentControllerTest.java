package com.healthcare.hospitalmanagementapi.unit.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.appointment.dto.*;
import com.healthcare.hospitalmanagementapi.appointment.service.AppointmentService;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import org.junit.jupiter.api.*;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    private UUID appointmentId;
    private AppointmentResponseDTO response;

    @BeforeEach
    void setUp() {
        appointmentId = UUID.randomUUID();

        response = AppointmentResponseDTO.builder()
                .id(appointmentId)
                .patientName("John Doe")
                .doctorName("Dr Smith")
                .build();
    }

    @Test
    void shouldReturnUnauthorized_whenCreateAppointmentWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser
    void shouldCreateAppointment_whenValidRequest() throws Exception {
        CreateAppointmentRequestDTO request = CreateAppointmentRequestDTO.builder()
                .patientId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .doctorTimeSlotId(UUID.randomUUID())
                .appointmentDate(LocalDate.now().plusDays(1))
                .build();

        when(appointmentService.createAppointment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/appointments/" + appointmentId))
                .andExpect(jsonPath("$.id").value(appointmentId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequest_whenCreateAppointmentWithInvalidBody() throws Exception {
        CreateAppointmentRequestDTO request = CreateAppointmentRequestDTO.builder().build();

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.patientId").exists())
                .andExpect(jsonPath("$.errors.doctorId").exists())
                .andExpect(jsonPath("$.errors.doctorTimeSlotId").exists())
                .andExpect(jsonPath("$.errors.appointmentDate").exists());
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenDoctorBlockedDate() throws Exception {
        CreateAppointmentRequestDTO request = CreateAppointmentRequestDTO.builder()
                .patientId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .doctorTimeSlotId(UUID.randomUUID())
                .appointmentDate(LocalDate.now().plusDays(1))
                .build();

        when(appointmentService.createAppointment(any()))
                .thenThrow(new ConflictException("Doctor is not available on selected date"));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Doctor is not available on selected date"));
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenDoctorTimeSlotBlocked() throws Exception {
        CreateAppointmentRequestDTO request = CreateAppointmentRequestDTO.builder()
                .patientId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .doctorTimeSlotId(UUID.randomUUID())
                .appointmentDate(LocalDate.now().plusDays(1))
                .build();

        when(appointmentService.createAppointment(any()))
                .thenThrow(new ConflictException("Selected doctor time slot is blocked for the selected date"));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Selected doctor time slot is blocked for the selected date"));
    }

    @Test
    @WithMockUser
    void shouldGetAppointmentById_whenAppointmentExists() throws Exception {
        when(appointmentService.getAppointmentById(appointmentId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/appointments/{id}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenAppointmentDoesNotExist() throws Exception {
        when(appointmentService.getAppointmentById(appointmentId))
                .thenThrow(new ResourceNotFoundException("Appointment not found"));

        mockMvc.perform(get("/api/v1/appointments/{id}", appointmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Appointment not found"));
    }

    @Test
    @WithMockUser
    void shouldGetAllAppointments_whenPaginationRequested() throws Exception {
        PageResponse<AppointmentResponseDTO> page = new PageResponse<>(
                new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1)
        );

        when(appointmentService.getAllAppointments(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/appointments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void shouldUpdateAppointment_whenValidRequest() throws Exception {
        UpdateAppointmentRequestDTO request = UpdateAppointmentRequestDTO.builder()
                .appointmentStatus(com.healthcare.hospitalmanagementapi.enums.AppointmentStatus.ADMITTED)
                .build();

        when(appointmentService.updateAppointment(eq(appointmentId), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/appointments/{id}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenInvalidAppointmentTransition() throws Exception {
        when(appointmentService.updateAppointment(eq(appointmentId), any()))
                .thenThrow(new ConflictException("Invalid status transition from ADMITTED to CONFIRMED"));

        mockMvc.perform(patch("/api/v1/appointments/{id}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentStatus\":\"CONFIRMED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Invalid status transition from ADMITTED to CONFIRMED"));
    }

    @Test
    @WithMockUser
    void shouldDeleteAppointment_whenAppointmentExists() throws Exception {
        doNothing().when(appointmentService).deleteAppointment(appointmentId);

        mockMvc.perform(delete("/api/v1/appointments/{id}", appointmentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldRestoreAppointment_whenDeletedAppointmentExists() throws Exception {
        when(appointmentService.restoreAppointment(appointmentId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/appointments/{id}/restore", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()));
    }

    @Test
    @WithMockUser
    void shouldReturnConflict_whenRestoreAlreadyActiveAppointment() throws Exception {
        when(appointmentService.restoreAppointment(appointmentId))
                .thenThrow(new ConflictException("Appointment is already active"));

        mockMvc.perform(post("/api/v1/appointments/{id}/restore", appointmentId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Appointment is already active"));
    }
}