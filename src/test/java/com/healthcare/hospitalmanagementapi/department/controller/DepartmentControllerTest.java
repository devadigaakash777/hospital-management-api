package com.healthcare.hospitalmanagementapi.department.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.department.dto.*;
import com.healthcare.hospitalmanagementapi.department.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateDepartment_whenValid() throws Exception {
        CreateDepartmentRequestDTO request = new CreateDepartmentRequestDTO("Cardiology");

        DepartmentResponseDTO response = new DepartmentResponseDTO(UUID.randomUUID(), "Cardiology");

        when(service.createDepartment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departmentName").value("Cardiology"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_whenNonAdminCreatesDepartment() throws Exception {
        CreateDepartmentRequestDTO request = new CreateDepartmentRequestDTO("Cardiology");

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorized_whenNoUserCreatesDepartment() throws Exception {
        CreateDepartmentRequestDTO request = new CreateDepartmentRequestDTO("Cardiology");

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequest_whenInvalidCreateRequest() throws Exception {
        CreateDepartmentRequestDTO request = new CreateDepartmentRequestDTO("");

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnConflict_whenDuplicateDepartment() throws Exception {
        CreateDepartmentRequestDTO request = new CreateDepartmentRequestDTO("Cardiology");

        when(service.createDepartment(any()))
                .thenThrow(new com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException("Department already exists"));

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @WithMockUser
    void shouldGetDepartmentById() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.getDepartmentById(id))
                .thenReturn(new DepartmentResponseDTO(id, "Cardiology"));

        mockMvc.perform(get("/api/v1/departments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentName").value("Cardiology"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFound_whenDepartmentMissing() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.getDepartmentById(id))
                .thenThrow(new com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException("Department not found"));

        mockMvc.perform(get("/api/v1/departments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateDepartment() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateDepartmentRequestDTO request = new UpdateDepartmentRequestDTO("Neurology");

        when(service.updateDepartment(eq(id), any()))
                .thenReturn(new DepartmentResponseDTO(id, "Neurology"));

        mockMvc.perform(patch("/api/v1/departments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentName").value("Neurology"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_whenNonAdminUpdates() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateDepartmentRequestDTO request = new UpdateDepartmentRequestDTO("Neurology");

        mockMvc.perform(patch("/api/v1/departments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteDepartment() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/departments/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).deleteDepartment(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_whenNonAdminDeletes() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/departments/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRestoreDepartment() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/departments/{id}/restore", id))
                .andExpect(status().isOk());

        verify(service).restoreDepartment(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_whenNonAdminRestores() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/departments/{id}/restore", id))
                .andExpect(status().isForbidden());
    }
}