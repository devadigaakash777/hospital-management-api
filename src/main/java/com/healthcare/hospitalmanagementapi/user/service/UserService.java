package com.healthcare.hospitalmanagementapi.user.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;

import java.util.UUID;

public interface UserService {

    UserResponseDTO createUser(CreateUserRequestDTO dto);

    UserResponseDTO getUserById(UUID id);

    UserResponseDTO getUserByEmail(String email);

    PageResponse<UserResponseDTO> getAllUsers(int page, int size);

    UserResponseDTO updateUser(UUID id, UpdateUserRequestDTO dto);

    void deleteUser(UUID id);

    public UserResponseDTO restoreUser(UUID id);

    public UserResponseDTO restoreUserByEmail(String email);

    void changePassword(UUID id, ChangePasswordRequestDTO dto);
}