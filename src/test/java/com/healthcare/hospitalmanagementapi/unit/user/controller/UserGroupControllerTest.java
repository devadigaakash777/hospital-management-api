package com.healthcare.hospitalmanagementapi.unit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.user.dto.group.*;
import com.healthcare.hospitalmanagementapi.user.service.UserGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserGroupService userGroupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_GROUPS")
    void shouldCreateUserGroup_whenValid() throws Exception {
        UUID id = UUID.randomUUID();

        CreateUserGroupRequestDTO request = CreateUserGroupRequestDTO.builder()
                .groupName("Admin")
                .build();

        UserGroupResponseDTO response = UserGroupResponseDTO.builder()
                .id(id)
                .groupName("Admin")
                .build();

        when(userGroupService.createUserGroup(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/user-groups")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/user-groups/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.groupName").value("Admin"));
    }

    @Test
    void shouldReturnUnauthorized_whenNoAuth() throws Exception {
        mockMvc.perform(post("/api/v1/user-groups")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_GROUPS")
    void shouldReturnBadRequest_whenInvalidInput() throws Exception {
        CreateUserGroupRequestDTO request = new CreateUserGroupRequestDTO();

        mockMvc.perform(post("/api/v1/user-groups")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @WithMockUser
    void shouldGetUserGroupById_whenValid() throws Exception {
        UUID id = UUID.randomUUID();

        UserGroupResponseDTO response = UserGroupResponseDTO.builder()
                .id(id)
                .groupName("Admin")
                .build();

        when(userGroupService.getUserGroupById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/user-groups/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_GROUPS")
    void shouldUpdateUserGroup_whenValid() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateUserGroupRequestDTO request = new UpdateUserGroupRequestDTO();
        request.setGroupName("Updated");

        UserGroupResponseDTO response = UserGroupResponseDTO.builder()
                .id(id)
                .groupName("Updated")
                .build();

        when(userGroupService.updateUserGroup(any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/user-groups/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupName").value("Updated"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_GROUPS"})
    void shouldDeleteUserGroup_whenAuthorized() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/user-groups/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "CAN_MANAGE_GROUPS")
    void shouldReturnForbidden_whenDeletingWithoutAdminRole() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/user-groups/" + id))
                .andExpect(status().isForbidden());
    }
}