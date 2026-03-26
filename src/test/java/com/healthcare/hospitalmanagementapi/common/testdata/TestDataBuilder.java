package com.healthcare.hospitalmanagementapi.common.testdata;

import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.dto.user.ChangePasswordRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.CreateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.UpdateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.UserResponseDTO;
import com.healthcare.hospitalmanagementapi.user.entity.User;

import java.util.HashSet;
import java.util.UUID;

public class TestDataBuilder {

    public static User user(UUID id) {
        User user = new User();
        user.setId(id);
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        user.setDepartments(new HashSet<>());
        return user;
    }

    public static CreateUserRequestDTO createUserDTO() {
        return CreateUserRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@example.com")
                .password("password123")
                .role(Role.STAFF)
                .build();
    }

    public static UpdateUserRequestDTO updateDTO(String email) {
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO();
        dto.setEmail(email);
        return dto;
    }

    public static ChangePasswordRequestDTO passwordDTO() {
        return ChangePasswordRequestDTO.builder()
                .oldPassword("oldPass")
                .newPassword("newPass123")
                .build();
    }

    public static UserResponseDTO response(UUID id) {
        return UserResponseDTO.builder()
                .id(id)
                .email("test@example.com")
                .firstName("John")
                .build();
    }
}