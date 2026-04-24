package com.healthcare.hospitalmanagementapi.unit.healthpackagetimeslot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.*;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageTimeSlotService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HealthPackageTimeSlotControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private HealthPackageTimeSlotService service;

    private UUID packageId;
    private UUID slotId;

    @BeforeEach
    void setUp() {
        packageId = UUID.randomUUID();
        slotId = UUID.randomUUID();
    }

    @Test
    void shouldReturnUnauthorized_whenCreateWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/health-packages/{id}/time-slots", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldCreateTimeSlot() throws Exception {
        HealthPackageTimeSlotResponseDTO response =
                HealthPackageTimeSlotResponseDTO.builder().id(slotId).build();

        when(service.create(any(), any())).thenReturn(response);

        CreateHealthPackageTimeSlotRequestDTO request =
                CreateHealthPackageTimeSlotRequestDTO.builder()
                        .startTime(LocalTime.of(10,0))
                        .endTime(LocalTime.of(11,0))
                        .totalSlots(5)
                        .build();

        mockMvc.perform(post("/api/v1/health-packages/{id}/time-slots", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(slotId.toString()));
    }

    @Test
    @WithMockUser
    void shouldGetAllSlots() throws Exception {
        when(service.getAllByHealthPackage(packageId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/health-packages/{id}/time-slots", packageId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetAvailableSlots() throws Exception {
        when(service.getAvailableSlots(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/health-packages/{id}/time-slots/available", packageId)
                        .param("appointmentDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldUpdateTimeSlot() throws Exception {
        when(service.update(any(), any(), any()))
                .thenReturn(HealthPackageTimeSlotResponseDTO.builder().id(slotId).build());

        mockMvc.perform(patch("/api/v1/health-packages/{id}/time-slots/{slotId}", packageId, slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldDeleteTimeSlot() throws Exception {
        doNothing().when(service).delete(packageId, slotId);

        mockMvc.perform(delete("/api/v1/health-packages/{id}/time-slots/{slotId}", packageId, slotId))
                .andExpect(status().isNoContent());
    }
}