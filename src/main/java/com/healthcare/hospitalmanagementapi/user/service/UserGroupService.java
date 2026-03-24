package com.healthcare.hospitalmanagementapi.user.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.user.dto.group.*;

import java.util.UUID;

public interface UserGroupService {

    UserGroupResponseDTO createUserGroup(CreateUserGroupRequestDTO dto);

    UserGroupResponseDTO getUserGroupById(UUID id);

    PageResponse<UserGroupResponseDTO> getAllUserGroups(int page, int size);

    UserGroupResponseDTO updateUserGroup(UUID id, UpdateUserGroupRequestDTO dto);

    void deleteUserGroup(UUID id);

    public UserGroupResponseDTO restoreUserGroup(UUID id);
}