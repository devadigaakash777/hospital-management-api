package com.healthcare.hospitalmanagementapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.support.WithMockAdmin;
import com.healthcare.hospitalmanagementapi.user.dto.user.CreateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.UpdateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailRequestDTO;
import com.healthcare.hospitalmanagementapi.user.entity.EmailVerificationToken;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import com.healthcare.hospitalmanagementapi.user.repository.EmailVerificationTokenRepository;
import com.healthcare.hospitalmanagementapi.user.repository.UserGroupRepository;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockAdmin
class UserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private UserGroupRepository userGroupRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private EmailVerificationTokenRepository tokenRepository;

    private Department department1;
    private Department department2;
    private UserGroup userGroup;

    @BeforeEach
    void setup() {
        department1 = departmentRepository.save(
                Department.builder().departmentName("Cardiology").build());

        department2 = departmentRepository.save(
                Department.builder().departmentName("Neurology").build());

        userGroup = userGroupRepository.save(
                UserGroup.builder()
                        .groupName("Staff Group")
                        .canManageStaff(true)
                        .canManageDoctorSlots(true)
                        .departments(Set.of(department1, department2))
                        .build());
    }

    private CreateUserRequestDTO validCreateRequest() {
        return CreateUserRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("Password123")
                .role(Role.STAFF)
                .groupId(userGroup.getId())
                .build();
    }

    private void initiateAndVerify(CreateUserRequestDTO request) throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        EmailVerificationToken token = tokenRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AssertionError("OTP token not saved for: " + request.getEmail()));

        VerifyEmailRequestDTO verifyDto = VerifyEmailRequestDTO.builder()
                .email(request.getEmail())
                .otp(token.getOtp())
                .build();

        mockMvc.perform(post("/api/v1/users/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isCreated());
    }

    private User savedUser(String email) {
        return userRepository.save(
                User.builder()
                        .firstName("Test").lastName("User")
                        .email(email).password("pass")
                        .role(Role.STAFF)
                        .build());
    }

    @Test
    void shouldCreateUser_afterOtpVerification() throws Exception {
        CreateUserRequestDTO request = validCreateRequest();

        initiateAndVerify(request);

        User saved = userRepository.findByEmailAndIsDeletedFalse("john.doe@test.com")
                .orElseThrow();

        assertThat(saved.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(saved.getGroup()).isNotNull();
        assertThat(saved.getDepartments()).hasSize(2);
        assertThat(tokenRepository.findByEmail("john.doe@test.com")).isEmpty();
    }

    @Test
    void shouldReturn202_andNotCreateUser_beforeOtpVerification() throws Exception {
        CreateUserRequestDTO request = validCreateRequest();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        assertThat(userRepository.findByEmailAndIsDeletedFalse("john.doe@test.com")).isEmpty();
        assertThat(tokenRepository.findByEmail("john.doe@test.com")).isPresent();
    }

    @Test
    void shouldReturn400_whenInvalidInput() throws Exception {
        CreateUserRequestDTO request = CreateUserRequestDTO.builder()
                .firstName("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409_whenEmailAlreadyExistsInUsers() throws Exception {
        initiateAndVerify(validCreateRequest());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void shouldReturn400_whenOtpIsWrong() throws Exception {
        CreateUserRequestDTO request = validCreateRequest();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        VerifyEmailRequestDTO badOtp = VerifyEmailRequestDTO.builder()
                .email(request.getEmail())
                .otp("000000")
                .build();

        mockMvc.perform(post("/api/v1/users/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badOtp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid OTP"));

        assertThat(userRepository.findByEmailAndIsDeletedFalse(request.getEmail())).isEmpty();
    }

    @Test
    void shouldReturn400_whenOtpIsExpired() throws Exception {
        CreateUserRequestDTO request = validCreateRequest();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        EmailVerificationToken token = tokenRepository
                .findByEmail(request.getEmail()).orElseThrow();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(token);

        VerifyEmailRequestDTO verifyDto = VerifyEmailRequestDTO.builder()
                .email(request.getEmail())
                .otp(token.getOtp())
                .build();

        mockMvc.perform(post("/api/v1/users/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("OTP has expired. Ask the administrator to resend."));
    }

    @Test
    void shouldResendOtp_andReplaceOldToken() throws Exception {
        CreateUserRequestDTO request = validCreateRequest();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        String firstOtp = tokenRepository.findByEmail(request.getEmail())
                .orElseThrow().getOtp();

        mockMvc.perform(post("/api/v1/users/resend-otp")
                        .param("email", request.getEmail()))
                .andExpect(status().isNoContent());

        String secondOtp = tokenRepository.findByEmail(request.getEmail())
                .orElseThrow().getOtp();

        assertThat(secondOtp).isNotEqualTo(firstOtp);
    }

    @Test
    void shouldGetUserById() throws Exception {
        User user = savedUser("jane@test.com");

        mockMvc.perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@test.com"));
    }

    @Test
    void shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateUser_andChangeDepartments() throws Exception {
        User user = savedUser("old@test.com");

        UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                .firstName("Updated")
                .departmentIds(Set.of(department1.getId()))
                .build();

        mockMvc.perform(patch("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getDepartments()).hasSize(1);
    }

    @Test
    void shouldSoftDeleteUser() throws Exception {
        User user = savedUser("delete@test.com");

        mockMvc.perform(delete("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findByIdAndIsDeletedFalse(user.getId())).isEmpty();
        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void shouldRestoreUser() throws Exception {
        User user = userRepository.save(
                User.builder()
                        .firstName("Restore").lastName("User")
                        .email("restore@test.com").password("pass")
                        .role(Role.STAFF).isDeleted(true)
                        .build());

        mockMvc.perform(post("/api/v1/users/restore")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk());

        User restored = userRepository.findById(user.getId()).orElseThrow();
        assertThat(restored.getIsDeleted()).isFalse();
        assertThat(restored.getDeletedAt()).isNull();
    }

    @Test
    void shouldGetAllUsers_withPagination() throws Exception {
        for (int i = 0; i < 5; i++) {
            savedUser("user" + i + "@test.com");
        }

        mockMvc.perform(get("/api/v1/users").param("page", "0").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(2));
    }
}