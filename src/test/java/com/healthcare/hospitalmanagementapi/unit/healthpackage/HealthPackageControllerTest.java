package com.healthcare.hospitalmanagementapi.unit.healthpackage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.*;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HealthPackageControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private HealthPackageService service;

    private UUID id;
    private HealthPackageResponseDTO response;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        response = HealthPackageResponseDTO.builder()
                .id(id)
                .packageName("Test Package")
                .build();
    }


    @Test
    void shouldReturnUnauthorized_whenCreateWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/health-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldCreateHealthPackage_whenValidRequest() throws Exception {

        CreateHealthPackageRequestDTO request = CreateHealthPackageRequestDTO.builder()
                .packageName("Test Package")
                .packagePrice(BigDecimal.TEN)
                .advanceBookingDays(30)
                .build();

        when(service.createHealthPackage(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/health-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/v1/health-packages/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldReturnBadRequest_whenCreateInvalidPayload() throws Exception {

        CreateHealthPackageRequestDTO request = CreateHealthPackageRequestDTO.builder().build();

        mockMvc.perform(post("/api/v1/health-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.packageName").exists())
                .andExpect(jsonPath("$.errors.packagePrice").exists())
                .andExpect(jsonPath("$.errors.advanceBookingDays").exists());
    }

    @Test
    @WithMockUser
    void shouldGetHealthPackageById_whenExists() throws Exception {

        when(service.getHealthPackageById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/health-packages/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser
    void shouldGetAllHealthPackages_withPagination() throws Exception {

        PageResponse<HealthPackageResponseDTO> page =
                new PageResponse<>(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        when(service.getAllHealthPackages(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/health-packages")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void shouldSearchHealthPackages() throws Exception {

        HealthPackageShortResponseDTO shortDto =
                HealthPackageShortResponseDTO.builder().id(id).packageName("Test").build();

        PageResponse<HealthPackageShortResponseDTO> page =
                new PageResponse<>(new PageImpl<>(List.of(shortDto), PageRequest.of(0, 10), 1));

        when(service.searchHealthPackages("test", 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/health-packages/search")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    @WithMockUser
    void shouldGetHealthPackageAvailability() throws Exception {

        HealthPackageAvailabilityResponseDTO availability =
                HealthPackageAvailabilityResponseDTO.builder()
                        .id(id)
                        .packageName("Test")
                        .build();

        when(service.getHealthPackageAvailability(id)).thenReturn(availability);

        mockMvc.perform(get("/api/v1/health-packages/{id}/availability", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldUpdateHealthPackage_whenValid() throws Exception {

        UpdateHealthPackageRequestDTO request =
                UpdateHealthPackageRequestDTO.builder()
                        .packageName("Updated")
                        .build();

        when(service.updateHealthPackage(eq(id), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/health-packages/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }


    @Test
    @WithMockUser(authorities = "CAN_MANAGE_HEALTH_PACKAGES")
    void shouldDeleteHealthPackage_whenExists() throws Exception {

        doNothing().when(service).deleteHealthPackage(id);

        mockMvc.perform(delete("/api/v1/health-packages/{id}", id))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_HEALTH_PACKAGES"})
    void shouldRestoreHealthPackage_whenDeleted() throws Exception {

        when(service.restoreHealthPackage(id)).thenReturn(response);

        mockMvc.perform(post("/api/v1/health-packages/{id}/restore", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }
}