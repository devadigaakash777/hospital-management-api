package com.healthcare.hospitalmanagementapi.unit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.auth.security.CustomAccessDeniedHandler;
import com.healthcare.hospitalmanagementapi.auth.security.JwtAuthenticationEntryPoint;
import com.healthcare.hospitalmanagementapi.auth.security.JwtAuthenticationFilter;
import com.healthcare.hospitalmanagementapi.common.exception.custom.BadRequestException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.config.SecurityConfig;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.controller.UserController;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;
import com.healthcare.hospitalmanagementapi.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // Required by SecurityConfig — mocked so WebMvcTest slice can wire up
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    private final UUID userId = UUID.randomUUID();

    private CreateUserRequestDTO validCreateRequest() {
        return CreateUserRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@example.com")
                .password("password123")
                .role(Role.STAFF)
                .build();
    }

    // ─── Security: POST /api/v1/users ───────────────────────────────────────────

    @Test
    @WithMockUser   // no CAN_MANAGE_STAFF
    void shouldReturn403_whenCreateUser_withoutPermission() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ─── Validation: POST /api/v1/users ─────────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldReturn400_whenCreateUser_withMissingFields() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequestDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verifyNoInteractions(userService);
    }

    // ─── Exception mapping: POST /api/v1/users ──────────────────────────────────

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldReturn409_whenCreateUser_withDuplicateEmail() throws Exception {
        doThrow(new ConflictException("Email already exists"))
                .when(userService).initiateUserCreation(any());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // ─── Validation: POST /api/v1/users/verify-email ────────────────────────────

    @Test
    void shouldReturn400_whenVerifyEmail_withInvalidOtpLength() throws Exception {
        VerifyEmailRequestDTO dto = VerifyEmailRequestDTO.builder()
                .email("test@example.com")
                .otp("123")   // too short — @Size(min=6, max=6)
                .build();

        mockMvc.perform(post("/api/v1/users/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verifyNoInteractions(userService);
    }

    // ─── Exception mapping: POST /api/v1/users/verify-email ─────────────────────

    @Test
    void shouldReturn400_whenVerifyEmail_withWrongOtp() throws Exception {
        VerifyEmailRequestDTO dto = VerifyEmailRequestDTO.builder()
                .email("test@example.com")
                .otp("000000")
                .build();

        when(userService.verifyEmail(any()))
                .thenThrow(new BadRequestException("Invalid OTP"));

        mockMvc.perform(post("/api/v1/users/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid OTP"));
    }

    @Test
    void shouldReturn400_whenVerifyEmail_withExpiredOtp() throws Exception {
        VerifyEmailRequestDTO dto = VerifyEmailRequestDTO.builder()
                .email("test@example.com")
                .otp("123456")
                .build();

        when(userService.verifyEmail(any()))
                .thenThrow(new BadRequestException("OTP has expired. Ask the administrator to resend."));

        mockMvc.perform(post("/api/v1/users/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("OTP has expired. Ask the administrator to resend."));
    }

    // ─── Security: POST /api/v1/users/resend-otp ────────────────────────────────

    @Test
    @WithMockUser   // no CAN_MANAGE_STAFF
    void shouldReturn403_whenResendOtp_withoutPermission() throws Exception {
        mockMvc.perform(post("/api/v1/users/resend-otp")
                        .param("email", "test@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ─── Exception mapping: POST /api/v1/users/resend-otp ───────────────────────

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldReturn404_whenResendOtp_withUnknownEmail() throws Exception {
        doThrow(new ResourceNotFoundException("No pending registration found for this email"))
                .when(userService).resendOtp("unknown@example.com");

        mockMvc.perform(post("/api/v1/users/resend-otp")
                        .param("email", "unknown@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No pending registration found for this email"));
    }

    // ─── Exception mapping: GET /api/v1/users/{id} ──────────────────────────────

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldReturn404_whenGetUserById_withUnknownId() throws Exception {
        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ─── Security: DELETE /api/v1/users/{id} ────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})   // missing ROLE_ADMIN
    void shouldReturn403_whenDeleteUser_withoutAdminRole() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ─── Validation: POST /api/v1/users/{id}/change-password ────────────────────

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldReturn400_whenChangePassword_withShortPassword() throws Exception {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .oldPassword("short")   // @Size(min=8) — fails before service
                .newPassword("newPassword123")
                .build();

        mockMvc.perform(post("/api/v1/users/{id}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verifyNoInteractions(userService);
    }

    // ─── Exception mapping: POST /api/v1/users/{id}/change-password ─────────────

    @Test
    @WithMockUser(authorities = {"CAN_MANAGE_STAFF"})
    void shouldReturn400_whenChangePassword_withWrongOldPassword() throws Exception {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .oldPassword("wrongPassword123")
                .newPassword("newPassword123")
                .build();

        doThrow(new BadRequestException("Old password is incorrect"))
                .when(userService).changePassword(eq(userId), any());

        mockMvc.perform(post("/api/v1/users/{id}/change-password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Old password is incorrect"));
    }
}