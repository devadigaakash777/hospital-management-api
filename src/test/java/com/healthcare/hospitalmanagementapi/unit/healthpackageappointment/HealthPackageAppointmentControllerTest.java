package com.healthcare.hospitalmanagementapi.unit.healthpackageappointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.*;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageAppointmentService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HealthPackageAppointmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private HealthPackageAppointmentService service;

    private UUID id;
    private HealthPackageAppointmentResponseDTO response;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        response = HealthPackageAppointmentResponseDTO.builder().id(id).build();
    }

    @Test
    void shouldReturnUnauthorized_whenCreateWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/health-package-appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldCreateAppointment_whenValid() throws Exception {
        CreateHealthPackageAppointmentRequestDTO request =
                CreateHealthPackageAppointmentRequestDTO.builder()
                        .patientId(UUID.randomUUID())
                        .healthPackageId(UUID.randomUUID())
                        .healthPackageTimeSlotId(UUID.randomUUID())
                        .appointmentDate(LocalDate.now().plusDays(1))
                        .build();

        when(service.createAppointment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/health-package-appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/v1/health-package-appointments/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser
    void shouldGetAppointmentById() throws Exception {
        when(service.getAppointmentById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/health-package-appointments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser
    void shouldGetAllAppointments() throws Exception {
        PageResponse<HealthPackageAppointmentResponseDTO> page =
                new PageResponse<>(new PageImpl<>(List.of(response), PageRequest.of(0,10),1));

        when(service.getAllAppointments(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/health-package-appointments")
                        .param("page","0")
                        .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    @WithMockUser
    void shouldUpdateAppointment() throws Exception {
        UpdateHealthPackageAppointmentRequestDTO request =
                UpdateHealthPackageAppointmentRequestDTO.builder().build();

        when(service.updateAppointment(eq(id), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/health-package-appointments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldDeleteAppointment() throws Exception {
        doNothing().when(service).deleteAppointment(id);

        mockMvc.perform(delete("/api/v1/health-package-appointments/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRestoreAppointment() throws Exception {
        when(service.restoreAppointment(id)).thenReturn(response);

        mockMvc.perform(post("/api/v1/health-package-appointments/{id}/restore", id))
                .andExpect(status().isOk());
    }
}