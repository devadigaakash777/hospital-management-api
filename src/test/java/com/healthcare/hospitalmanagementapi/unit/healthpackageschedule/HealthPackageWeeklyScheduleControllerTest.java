package com.healthcare.hospitalmanagementapi.unit.healthpackageschedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.*;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageWeeklyScheduleService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HealthPackageWeeklyScheduleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private HealthPackageWeeklyScheduleService service;

    private UUID packageId;

    @BeforeEach
    void setUp() {
        packageId = UUID.randomUUID();
    }

    @Test
    void shouldReturnUnauthorized_whenBulkCreateWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/health-packages/{id}/weekly-schedules/bulk", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldBulkCreateSchedules() throws Exception {
        when(service.bulkCreate(any(), any())).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/health-packages/{id}/weekly-schedules/bulk", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldGetAllSchedules() throws Exception {
        when(service.getAllByHealthPackage(packageId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/health-packages/{id}/weekly-schedules", packageId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetById() throws Exception {
        UUID scheduleId = UUID.randomUUID();

        when(service.getById(packageId, scheduleId))
                .thenReturn(HealthPackageWeeklyScheduleResponseDTO.builder().build());

        mockMvc.perform(get("/api/v1/health-packages/{id}/weekly-schedules/{sid}", packageId, scheduleId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldBulkUpdateSchedules() throws Exception {
        when(service.bulkUpdate(any(), any())).thenReturn(List.of());

        mockMvc.perform(put("/api/v1/health-packages/{id}/weekly-schedules/bulk", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldBulkDeleteSchedules() throws Exception {
        doNothing().when(service).bulkDelete(any(), any());

        mockMvc.perform(delete("/api/v1/health-packages/{id}/weekly-schedules/bulk", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isNoContent());
    }
}