package com.healthcare.hospitalmanagementapi.unit.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.exception.custom.*;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.unit.testdata.TestDataBuilder;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;
import com.healthcare.hospitalmanagementapi.user.entity.EmailVerificationToken;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.mapper.UserMapper;
import com.healthcare.hospitalmanagementapi.user.repository.EmailVerificationTokenRepository;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import com.healthcare.hospitalmanagementapi.user.service.impl.EmailService;
import com.healthcare.hospitalmanagementapi.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private UserServiceImpl userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = TestDataBuilder.user(userId);

        // inject @Value field — Spring doesn't run in pure Mockito tests
        ReflectionTestUtils.setField(userService, "otpExpiryMinutes", 10);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── initiateUserCreation ────────────────────────────────────────────────────

    @Test
    void shouldInitiateUserCreation_whenValidInput() throws Exception {
        mockAdminAuth();

        CreateUserRequestDTO dto = TestDataBuilder.createUserDTO();

        when(userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())).thenReturn(false);
        when(tokenRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(dto)).thenReturn("{\"email\":\"test@example.com\"}");

        userService.initiateUserCreation(dto);

        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendOtpEmail(eq(dto.getEmail()), anyString());
        verify(userRepository, never()).save(any());   // user must NOT be saved yet
    }

    @Test
    void shouldDeleteOldToken_whenEmailAlreadyHasPendingOtp() throws Exception {
        mockAdminAuth();

        CreateUserRequestDTO dto = TestDataBuilder.createUserDTO();
        EmailVerificationToken oldToken = EmailVerificationToken.builder()
                .email(dto.getEmail())
                .otp("111111")
                .payload("{}")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())).thenReturn(false);
        when(tokenRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(oldToken));
        when(objectMapper.writeValueAsString(dto)).thenReturn("{}");

        userService.initiateUserCreation(dto);

        verify(tokenRepository).delete(oldToken);     // old token removed first
        verify(tokenRepository).save(any(EmailVerificationToken.class));  // new one saved
    }

    @Test
    void shouldThrowConflict_whenEmailAlreadyExistsInUsers() {
        mockAdminAuth();

        CreateUserRequestDTO dto = TestDataBuilder.createUserDTO();
        when(userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.initiateUserCreation(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendOtpEmail(any(), any());
    }

    @Test
    void shouldThrowConflict_whenNonAdminTriesToCreateAdmin() {
        mockNonAdminAuth();

        CreateUserRequestDTO dto = TestDataBuilder.createUserDTO();
        dto.setRole(Role.ADMIN);

        assertThatThrownBy(() -> userService.initiateUserCreation(dto))
                .isInstanceOf(ConflictException.class);

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendOtpEmail(any(), any());
    }

    // ─── verifyEmail ─────────────────────────────────────────────────────────────

    @Test
    void shouldCreateUser_whenOtpIsValid() throws Exception {
        CreateUserRequestDTO dto = TestDataBuilder.createUserDTO();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(dto.getEmail())
                .otp("123456")
                .payload("{}")
                .expiresAt(LocalDateTime.now().plusMinutes(5))   // not expired
                .build();

        VerifyEmailRequestDTO verifyDto = VerifyEmailRequestDTO.builder()
                .email(dto.getEmail())
                .otp("123456")
                .build();

        when(tokenRepository.findByEmailAndOtp(verifyDto.getEmail(), verifyDto.getOtp()))
                .thenReturn(Optional.of(token));
        when(objectMapper.readValue(token.getPayload(), CreateUserRequestDTO.class))
                .thenReturn(dto);
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(TestDataBuilder.response(userId));

        UserResponseDTO result = userService.verifyEmail(verifyDto);

        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).save(user);            // user created only now
        verify(tokenRepository).delete(token);        // token consumed after success
    }

    @Test
    void shouldThrowBadRequest_whenOtpIsWrong() {
        VerifyEmailRequestDTO verifyDto = VerifyEmailRequestDTO.builder()
                .email("test@example.com")
                .otp("000000")
                .build();

        when(tokenRepository.findByEmailAndOtp(verifyDto.getEmail(), verifyDto.getOtp()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmail(verifyDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid OTP");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequest_whenOtpIsExpired() {
        VerifyEmailRequestDTO verifyDto = VerifyEmailRequestDTO.builder()
                .email("test@example.com")
                .otp("123456")
                .build();

        EmailVerificationToken expiredToken = EmailVerificationToken.builder()
                .email(verifyDto.getEmail())
                .otp("123456")
                .payload("{}")
                .expiresAt(LocalDateTime.now().minusMinutes(1))  // already expired
                .build();

        when(tokenRepository.findByEmailAndOtp(verifyDto.getEmail(), verifyDto.getOtp()))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> userService.verifyEmail(verifyDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");

        verify(tokenRepository).delete(expiredToken); // expired token cleaned up
        verify(userRepository, never()).save(any());
    }

    // ─── resendOtp ───────────────────────────────────────────────────────────────

    @Test
    void shouldResendOtp_whenPendingTokenExists() {
        EmailVerificationToken existing = EmailVerificationToken.builder()
                .email("test@example.com")
                .otp("111111")
                .payload("{}")
                .expiresAt(LocalDateTime.now().plusMinutes(2))
                .build();

        when(tokenRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existing));
        when(tokenRepository.save(existing)).thenReturn(existing);

        userService.resendOtp("test@example.com");

        verify(tokenRepository).save(existing);
        verify(emailService).sendOtpEmail(eq("test@example.com"), anyString());
        // OTP and expiresAt should have been refreshed
        assertThat(existing.getOtp()).isNotEqualTo("111111");
        assertThat(existing.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void shouldThrowNotFound_whenNoTokenExistsForResend() {
        when(tokenRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resendOtp("unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No pending registration");

        verify(emailService, never()).sendOtpEmail(any(), any());
    }

    // ─── getUserById ─────────────────────────────────────────────────────────────

    @Test
    void shouldReturnUser_whenFound() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(TestDataBuilder.response(userId));

        UserResponseDTO result = userService.getUserById(userId);

        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ─── updateUser ──────────────────────────────────────────────────────────────

    @Test
    void shouldUpdateUser_whenValidInput() {
        mockAdminAuth();
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
    void shouldThrowConflict_whenUpdateEmailAlreadyExists() {
        mockAdminAuth();
        UpdateUserRequestDTO dto = TestDataBuilder.updateDTO("new@mail.com");

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, dto))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    // ─── deleteUser ──────────────────────────────────────────────────────────────

    @Test
    void shouldDeleteUser_whenExists() {
        mockAdminAuth();
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowNotFound_whenDeleteUserDoesNotExist() {
        mockAdminAuth();
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }

    // ─── restoreUser ─────────────────────────────────────────────────────────────

    @Test
    void shouldRestoreUser_whenDeleted() {
        user.setIsDeleted(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(TestDataBuilder.response(userId));

        UserResponseDTO result = userService.restoreUser(userId);

        assertThat(result).isNotNull();
        assertThat(user.getIsDeleted()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
    }

    @Test
    void shouldThrowConflict_whenUserAlreadyActive() {
        user.setIsDeleted(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.restoreUser(userId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already active");
    }

    // ─── changePassword ──────────────────────────────────────────────────────────

    @Test
    void shouldChangePassword_whenOldPasswordMatches() {
        ChangePasswordRequestDTO dto = TestDataBuilder.passwordDTO();

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(dto.getNewPassword())).thenReturn("encoded");

        userService.changePassword(userId, dto);

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("encoded");
    }

    @Test
    void shouldThrowBadRequest_whenOldPasswordIsIncorrect() {
        ChangePasswordRequestDTO dto = TestDataBuilder.passwordDTO();

        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(userId, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Old password is incorrect");

        verify(userRepository, never()).save(any());
    }

    // ─── getAllUsers ─────────────────────────────────────────────────────────────

    @Test
    void shouldReturnPaginatedUsers_withCorrectMetadata() {
        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userRepository.findAllByIsDeletedFalse(any())).thenReturn(page);
        when(userMapper.toResponseDTO(user)).thenReturn(TestDataBuilder.response(userId));

        PageResponse<UserResponseDTO> response = userService.getAllUsers(0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getPageNumber()).isZero();
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }

    // ─── Auth helpers ────────────────────────────────────────────────────────────

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