package com.healthcare.hospitalmanagementapi.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.exception.custom.BadRequestException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailChangeRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;
import com.healthcare.hospitalmanagementapi.user.entity.EmailVerificationToken;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import com.healthcare.hospitalmanagementapi.user.mapper.UserMapper;
import com.healthcare.hospitalmanagementapi.user.repository.EmailVerificationTokenRepository;
import com.healthcare.hospitalmanagementapi.user.repository.UserGroupRepository;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import com.healthcare.hospitalmanagementapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    @Override
    @Transactional
    public void initiateUserCreation(CreateUserRequestDTO dto) {
        validateAdminAssignment(dto.getRole());

        if (userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        tokenRepository.findByEmail(dto.getEmail())
                .ifPresent(tokenRepository::delete);

        String otp = generateOtp();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid request data");
        }

        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(dto.getEmail())
                .otp(otp)
                .payload(payload)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        tokenRepository.save(token);
        emailService.sendOtpEmail(dto.getEmail(), otp);

        log.info("OTP initiated for pending user: {}", dto.getEmail());
    }

    @Override
    @Transactional
    public UserResponseDTO verifyEmail(VerifyEmailRequestDTO dto) {

        EmailVerificationToken token = tokenRepository
                .findByEmailAndOtp(dto.getEmail(), dto.getOtp())
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        if (token.getUserId() != null) {
            throw new BadRequestException("Invalid OTP");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new BadRequestException("OTP has expired. Ask the administrator to resend.");
        }

        CreateUserRequestDTO original;
        try {
            original = objectMapper.readValue(token.getPayload(), CreateUserRequestDTO.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Stored request is corrupted. Ask admin to recreate.");
        }

        User user = userMapper.toEntity(original);
        user.setPassword(passwordEncoder.encode(original.getPassword()));
        applyGroupOrUserLogic(user, original.getDepartmentIds(), original.getGroupId());

        User saved = userRepository.save(user);

        tokenRepository.delete(token);

        log.info("User created after email verification: {}", saved.getId());
        return userMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void resendOtp(String email) {

        EmailVerificationToken existing = tokenRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No pending registration found for this email"));

        String newOtp = generateOtp();
        existing.setOtp(newOtp);
        existing.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        tokenRepository.save(existing);

        emailService.sendOtpEmail(email, newOtp);

        log.info("OTP resent for email: {}", email);
    }

    @Override
    @Transactional
    public void initiateEmailChange(UUID userId, ChangeEmailRequestDTO dto) {

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));

        String newEmail = dto.getNewEmail();

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new BadRequestException("New email must be different from the current email");
        }

        if (userRepository.existsByEmailAndIsDeletedFalse(newEmail)) {
            throw new ConflictException("Email already in use");
        }

        tokenRepository.findByUserId(userId)
                .ifPresent(tokenRepository::delete);

        tokenRepository.findByEmail(newEmail)
                .filter(t -> t.getUserId() == null)
                .ifPresent(tokenRepository::delete);

        String otp = generateOtp();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(newEmail)
                .otp(otp)
                .userId(userId)
                .pendingEmail(newEmail)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        tokenRepository.save(token);
        emailService.sendOtpEmail(newEmail, otp);

        log.info("Email-change OTP sent to {} for user {}", newEmail, userId);
    }

    @Override
    @Transactional
    @CachePut(key = "#dto.userId")
    public UserResponseDTO verifyEmailChange(VerifyEmailChangeRequestDTO dto) {

        EmailVerificationToken token = tokenRepository
                .findByEmailAndOtpAndUserId(dto.getNewEmail(), dto.getOtp(), dto.getUserId())
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));

        if (userRepository.existsByEmailAndIsDeletedFalse(dto.getNewEmail())) {
            tokenRepository.delete(token);
            throw new ConflictException("Email already in use");
        }

        user.setEmail(dto.getNewEmail());
        User saved = userRepository.save(user);

        tokenRepository.delete(token);

        log.info("Email updated for user {}: {}", dto.getUserId(), dto.getNewEmail());
        return userMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public UserResponseDTO getUserById(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findAllByIsDeletedFalse(pageable);
        return new PageResponse<>(userPage.map(userMapper::toResponseDTO));
    }

    @Override
    @CachePut(key = "#id")
    public UserResponseDTO updateUser(UUID id, UpdateUserRequestDTO dto) {

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));

        validateAdminAccessForTarget(user);
        validateAdminAssignment(dto.getRole());

        userMapper.updateUserFromDTO(dto, user);
        applyGroupOrUserLogic(user, dto.getDepartmentIds(), dto.getGroupId());

        User updated = userRepository.save(user);
        log.info("User updated with id: {}", id);
        return userMapper.toResponseDTO(updated);
    }

    @Override
    @CacheEvict(key = "#id")
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
        validateAdminAccessForTarget(user);
        userRepository.delete(user);
        log.info("User soft deleted with id: {}", id);
    }

    @Override
    public UserResponseDTO restoreUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
        return restoreUser(user.getId());
    }

    @Override
    @CachePut(key = "#id")
    public UserResponseDTO restoreUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));

        if (!Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ConflictException("User is already active and cannot be restored");
        }

        user.setIsDeleted(false);
        user.setDeletedAt(null);

        User saved = userRepository.save(user);
        log.info("User restored with id: {}", id);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    public void changePassword(UUID id, ChangePasswordRequestDTO dto) {

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDTO> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.searchUsers(keyword, pageable);
        return new PageResponse<>(users.map(userMapper::toResponseDTO));
    }

    private String generateOtp() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
    }

    private void applyGroupOrUserLogic(User user, Set<UUID> departmentIds, UUID groupId) {
        if (groupId != null) {
            applyGroupLogic(user, groupId);
        } else {
            applyDepartmentLogic(user, departmentIds);
        }
    }

    private void applyGroupLogic(User user, UUID groupId) {
        UserGroup group = userGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        user.setGroup(group);
        user.setDepartments(new HashSet<>(group.getDepartments()));
        user.setCanManageDoctorSlots(group.getCanManageDoctorSlots());
        user.setCanManageStaff(group.getCanManageStaff());
        user.setCanManageGroups(group.getCanManageGroups());
        user.setCanExportReports(group.getCanExportReports());
        user.setCanManageHealthPackages(group.getCanManageHealthPackages());
    }

    private void applyDepartmentLogic(User user, Set<UUID> departmentIds) {
        user.setGroup(null);

        if (departmentIds == null) return;

        if (departmentIds.isEmpty()) {
            if (user.getDepartments() == null) {
                user.setDepartments(new HashSet<>());
            } else {
                user.getDepartments().clear();
            }
            return;
        }

        Set<Department> departments = new HashSet<>(
                departmentRepository.findAllById(departmentIds)
        );

        if (departments.size() != departmentIds.size()) {
            throw new ResourceNotFoundException("One or more departments not found");
        }

        user.setDepartments(departments);
    }

    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            return customUserDetails;
        }

        throw new ConflictException("Authenticated user not found");
    }

    private void validateAdminAccessForTarget(User targetUser) {
        CustomUserDetails currentUser = getCurrentUser();

        if (targetUser.getRole() == Role.ADMIN && currentUser.getRole() != Role.ADMIN) {
            throw new ConflictException("Only ADMIN can modify ADMIN user");
        }
    }

    private void validateAdminAssignment(Role role) {
        if (role == Role.ADMIN) {
            CustomUserDetails currentUser = getCurrentUser();

            if (currentUser.getRole() != Role.ADMIN) {
                throw new ConflictException("Only ADMIN can assign ADMIN role");
            }
        }
    }
}