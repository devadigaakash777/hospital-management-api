package com.healthcare.hospitalmanagementapi.user.service;

import com.healthcare.hospitalmanagementapi.common.exception.custom.*;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.common.testdata.TestDataBuilder;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import com.healthcare.hospitalmanagementapi.user.mapper.UserMapper;
import com.healthcare.hospitalmanagementapi.user.repository.UserGroupRepository;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import com.healthcare.hospitalmanagementapi.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private UserGroupRepository userGroupRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = TestDataBuilder.user(userId);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateUser_whenValidInput() {
        mockAdminAuth();

        CreateUserRequestDTO dto = TestDataBuilder.createUserDTO();

        when(userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(TestDataBuilder.response(userId));

        UserResponseDTO result = userService.createUser(dto);

        assertThat(result.getId()).isEqualTo(userId);

        verify(userRepository).save(user);
        verify(passwordEncoder).encode(dto.getPassword());
        verify(userMapper).toResponseDTO(user);
    }

    @Test
    void shouldThrowConflict_whenEmailAlreadyExists() {
        mockAdminAuth();

        CreateUserRequestDTO dto = TestDataBuilder.createUserDTO();

        when(userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflict_whenNonAdminCreatesAdmin() {
        mockNonAdminAuth();

        CreateUserRequestDTO dto = TestDataBuilder.createUserDTO();
        dto.setRole(Role.ADMIN);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldUpdateUser_whenValidInput() {
        UpdateUserRequestDTO dto = TestDataBuilder.updateDTO("new@mail.com");

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(TestDataBuilder.response(userId));

        UserResponseDTO result = userService.updateUser(userId, dto);

        assertThat(result).isNotNull();
        verify(userMapper).updateUserFromDTO(dto, user);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowConflict_whenUpdateEmailExists() {
        UpdateUserRequestDTO dto = TestDataBuilder.updateDTO("new@mail.com");

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldDeleteUser_whenExists() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void shouldRestoreUser_whenDeleted() {
        user.setIsDeleted(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(TestDataBuilder.response(userId));

        UserResponseDTO result = userService.restoreUser(userId);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowConflict_whenUserAlreadyActive() {
        user.setIsDeleted(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.restoreUser(userId))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldChangePassword_whenValidOldPassword() {
        ChangePasswordRequestDTO dto = TestDataBuilder.passwordDTO();

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(dto.getNewPassword())).thenReturn("encoded");

        userService.changePassword(userId, dto);

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowBadRequest_whenOldPasswordIncorrect() {
        ChangePasswordRequestDTO dto = TestDataBuilder.passwordDTO();

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(userId, dto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldReturnPaginatedUsers_withMetadata() {
        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userRepository.findAllByIsDeletedFalse(any())).thenReturn(page);
        when(userMapper.toResponseDTO(user)).thenReturn(TestDataBuilder.response(userId));

        PageResponse<UserResponseDTO> response = userService.getAllUsers(0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);

        assertThat(response.getPageNumber()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }

    private void mockAdminAuth() {
        setAuth(Role.ADMIN);
    }

    private void mockNonAdminAuth() {
        setAuth(Role.STAFF);
    }

    private void setAuth(Role role) {
        User u = new User();
        u.setRole(role);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(
                new com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails(u)
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}