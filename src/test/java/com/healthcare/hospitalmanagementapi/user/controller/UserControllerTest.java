package com.healthcare.hospitalmanagementapi.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.dto.user.ChangePasswordRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.CreateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.UserResponseDTO;
import com.healthcare.hospitalmanagementapi.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final UUID userId = UUID.randomUUID();

    private UserResponseDTO responseDTO() {
        return UserResponseDTO.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .build();
    }

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldCreateUser_whenValidRequest() throws Exception {
        CreateUserRequestDTO dto = CreateUserRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@example.com")
                .password("password123")
                .role(Role.STAFF)
                .build();

        when(userService.createUser(any())).thenReturn(responseDTO());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldReturnValidationError_whenInvalidRequest() throws Exception {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldGetUserById() throws Exception {
        when(userService.getUserById(userId)).thenReturn(responseDTO());

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAN_MANAGE_STAFF"})
    void shouldDeleteUser_whenAuthorized() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldChangePassword() throws Exception {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .oldPassword("oldPassword123")
                .newPassword("newPassword123")
                .build();

        doNothing().when(userService).changePassword(eq(userId), any());

        mockMvc.perform(post("/api/v1/users/{id}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq(userId), any());
    }

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldReturnConflict_whenServiceThrows() throws Exception {
        when(userService.getUserById(userId))
                .thenThrow(new com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException("Conflict"));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Conflict"));
    }
}