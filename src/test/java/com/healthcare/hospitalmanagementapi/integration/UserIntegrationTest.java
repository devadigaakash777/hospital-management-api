package com.healthcare.hospitalmanagementapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.dto.user.CreateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.UpdateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
@WithMockUser(
        username = "admin@test.com",
        authorities = {
                "ROLE_ADMIN",
                "CAN_MANAGE_STAFF",
                "CAN_MANAGE_DOCTOR_SLOTS"
        }
)
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department1;
    private Department department2;
    private UserGroup userGroup;

    @BeforeEach
    void setup() {
        department1 = departmentRepository.save(
                Department.builder().departmentName("Cardiology").build()
        );

        department2 = departmentRepository.save(
                Department.builder().departmentName("Neurology").build()
        );

        userGroup = userGroupRepository.save(
                UserGroup.builder()
                        .groupName("Staff Group")
                        .canManageStaff(true)
                        .canManageDoctorSlots(true)
                        .departments(Set.of(department1, department2))
                        .build()
        );
    }

    private CreateUserRequestDTO createValidUserRequest() {
        return CreateUserRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("Password123")
                .role(Role.ADMIN)
                .groupId(userGroup.getId())
                .build();
    }

    @Test
    void shouldCreateUser_withGroupAndDepartments() throws Exception {

        CreateUserRequestDTO request = createValidUserRequest();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value("john.doe@test.com"));

        User saved = userRepository.findAll().get(0);

        assertThat(saved.getGroup()).isNotNull();
        assertThat(saved.getDepartments()).hasSize(2);
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
    void shouldReturn409_whenDuplicateEmail() throws Exception {

        CreateUserRequestDTO request = createValidUserRequest();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetUserById() throws Exception {

        User user = userRepository.save(
                User.builder()
                        .firstName("Jane")
                        .lastName("Doe")
                        .email("jane@test.com")
                        .password("pass")
                        .role(Role.STAFF)
                        .build()
        );

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

        User user = userRepository.save(
                User.builder()
                        .firstName("Old")
                        .lastName("Name")
                        .email("old@test.com")
                        .password("pass")
                        .role(Role.STAFF)
                        .build()
        );

        UpdateUserRequestDTO request = UpdateUserRequestDTO.builder()
                .firstName("Updated")
                .departmentIds(Set.of(department1.getId()))
                .build();

        mockMvc.perform(patch("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).orElseThrow();

        assertThat(updated.getDepartments()).hasSize(1);
    }

    @Test
    void shouldSoftDeleteUser() throws Exception {

        User user = userRepository.save(
                User.builder()
                        .firstName("Delete")
                        .lastName("User")
                        .email("delete@test.com")
                        .password("pass")
                        .role(Role.STAFF)
                        .build()
        );

        mockMvc.perform(delete("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void shouldRestoreUser() throws Exception {

        User user = userRepository.save(
                User.builder()
                        .firstName("Restore")
                        .lastName("User")
                        .email("restore@test.com")
                        .password("pass")
                        .role(Role.STAFF)
                        .isDeleted(true)
                        .build()
        );

        mockMvc.perform(post("/api/v1/users/restore")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk());

        User restored = userRepository.findById(user.getId()).orElseThrow();

        assertThat(restored.getIsDeleted()).isFalse();
    }

    @Test
    void shouldGetAllUsers_withPagination() throws Exception {

        for (int i = 0; i < 5; i++) {
            userRepository.save(
                    User.builder()
                            .firstName("User" + i)
                            .lastName("Test")
                            .email("user" + i + "@test.com")
                            .password("pass")
                            .role(Role.STAFF)
                            .build()
            );
        }

        mockMvc.perform(get("/api/v1/users?page=0&size=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }
}