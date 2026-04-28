package com.healthcare.hospitalmanagementapi.user.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailChangeRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;

import java.util.UUID;

public interface UserService {

    void initiateUserCreation(CreateUserRequestDTO dto);

    UserResponseDTO verifyEmail(VerifyEmailRequestDTO dto);

    void resendOtp(String email);

    UserResponseDTO getUserById(UUID id);

    PageResponse<UserResponseDTO> getAllUsers(int page, int size);

    UserResponseDTO updateUser(UUID id, UpdateUserRequestDTO dto);

    void deleteUser(UUID id);

    UserResponseDTO restoreUser(UUID id);

    UserResponseDTO restoreUserByEmail(String email);

    void changePassword(UUID id, ChangePasswordRequestDTO dto);

    PageResponse<UserResponseDTO> searchUsers(String keyword, int page, int size);

    void initiateEmailChange(UUID userId, ChangeEmailRequestDTO dto);

    UserResponseDTO verifyEmailChange(VerifyEmailChangeRequestDTO dto);
}