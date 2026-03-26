package com.healthcare.hospitalmanagementapi.user.service;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.user.dto.group.*;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import com.healthcare.hospitalmanagementapi.user.mapper.UserGroupMapper;
import com.healthcare.hospitalmanagementapi.user.repository.UserGroupRepository;
import com.healthcare.hospitalmanagementapi.user.service.impl.UserGroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGroupServiceImplTest {

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserGroupMapper userGroupMapper;

    @InjectMocks
    private UserGroupServiceImpl userGroupService;

    private UUID groupId;
    private CreateUserGroupRequestDTO createDTO;
    private UserGroup userGroup;
    private UserGroupResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        groupId = UUID.randomUUID();

        createDTO = CreateUserGroupRequestDTO.builder()
                .groupName("Admin")
                .departmentIds(List.of(UUID.randomUUID()))
                .build();

        userGroup = new UserGroup();
        userGroup.setId(groupId);
        userGroup.setGroupName("Admin");

        responseDTO = UserGroupResponseDTO.builder()
                .id(groupId)
                .groupName("Admin")
                .build();
    }

    @Test
    void shouldCreateUserGroup_whenValidInput() {
        UUID deptId = createDTO.getDepartmentIds().iterator().next();

        Department dept = new Department();
        dept.setId(deptId);

        when(userGroupRepository.existsByGroupNameAndIsDeletedFalse("Admin")).thenReturn(false);
        when(departmentRepository.findAllByIdInAndIsDeletedFalse(any()))
                .thenReturn(List.of(dept));
        when(userGroupMapper.toEntity(createDTO)).thenReturn(userGroup);
        when(userGroupRepository.save(userGroup)).thenReturn(userGroup);
        when(userGroupMapper.toResponseDTO(userGroup)).thenReturn(responseDTO);

        UserGroupResponseDTO result = userGroupService.createUserGroup(createDTO);

        assertThat(result.getId()).isEqualTo(groupId);

        verify(userGroupRepository).save(userGroup);
        verify(userGroupMapper).toResponseDTO(userGroup);
    }

    @Test
    void shouldThrowConflict_whenGroupNameExists() {
        when(userGroupRepository.existsByGroupNameAndIsDeletedFalse("Admin")).thenReturn(true);

        assertThatThrownBy(() -> userGroupService.createUserGroup(createDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Group name already exists");
    }

    @Test
    void shouldThrowConflict_whenDuplicateDepartmentIds() {
        UUID deptId = UUID.randomUUID();
        createDTO.setDepartmentIds(List.of(deptId, deptId));

        assertThatThrownBy(() -> userGroupService.createUserGroup(createDTO))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldThrowNotFound_whenDepartmentMissing() {
        when(userGroupRepository.existsByGroupNameAndIsDeletedFalse("Admin")).thenReturn(false);
        when(departmentRepository.findAllByIdInAndIsDeletedFalse(any()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> userGroupService.createUserGroup(createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnUserGroup_whenFoundById() {
        when(userGroupRepository.findByIdAndIsDeletedFalse(groupId))
                .thenReturn(Optional.of(userGroup));
        when(userGroupMapper.toResponseDTO(userGroup)).thenReturn(responseDTO);

        UserGroupResponseDTO result = userGroupService.getUserGroupById(groupId);

        assertThat(result.getId()).isEqualTo(groupId);
    }

    @Test
    void shouldThrowNotFound_whenUserGroupNotFound() {
        when(userGroupRepository.findByIdAndIsDeletedFalse(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userGroupService.getUserGroupById(groupId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnPaginatedUserGroups() {
        Page<UserGroup> page = new PageImpl<>(List.of(userGroup));

        when(userGroupRepository.findAllByIsDeletedFalse(any()))
                .thenReturn(page);
        when(userGroupMapper.toResponseDTO(userGroup)).thenReturn(responseDTO);

        var result = userGroupService.getAllUserGroups(0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldUpdateUserGroup_whenValid() {
        UpdateUserGroupRequestDTO dto = new UpdateUserGroupRequestDTO();
        dto.setGroupName("Updated");

        when(userGroupRepository.findByIdAndIsDeletedFalse(groupId))
                .thenReturn(Optional.of(userGroup));
        when(userGroupRepository.existsByGroupNameAndIsDeletedFalse("Updated"))
                .thenReturn(false);
        when(userGroupRepository.save(userGroup)).thenReturn(userGroup);
        when(userGroupMapper.toResponseDTO(userGroup)).thenReturn(responseDTO);

        UserGroupResponseDTO result = userGroupService.updateUserGroup(groupId, dto);

        assertThat(result).isNotNull();

        verify(userGroupMapper).updateUserGroupFromDTO(dto, userGroup);
    }

    @Test
    void shouldThrowConflict_whenUpdatingWithExistingName() {
        UpdateUserGroupRequestDTO dto = new UpdateUserGroupRequestDTO();
        dto.setGroupName("New");

        when(userGroupRepository.findByIdAndIsDeletedFalse(groupId))
                .thenReturn(Optional.of(userGroup));
        when(userGroupRepository.existsByGroupNameAndIsDeletedFalse("New"))
                .thenReturn(true);

        assertThatThrownBy(() -> userGroupService.updateUserGroup(groupId, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldDeleteUserGroup_whenExists() {
        when(userGroupRepository.findByIdAndIsDeletedFalse(groupId))
                .thenReturn(Optional.of(userGroup));

        userGroupService.deleteUserGroup(groupId);

        verify(userGroupRepository).delete(userGroup);
    }

    @Test
    void shouldThrowNotFound_whenDeletingNonExisting() {
        when(userGroupRepository.findByIdAndIsDeletedFalse(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userGroupService.deleteUserGroup(groupId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRestoreUserGroup_whenDeleted() {
        userGroup.setIsDeleted(true);

        when(userGroupRepository.findById(groupId)).thenReturn(Optional.of(userGroup));
        when(userGroupRepository.save(userGroup)).thenReturn(userGroup);
        when(userGroupMapper.toResponseDTO(userGroup)).thenReturn(responseDTO);

        UserGroupResponseDTO result = userGroupService.restoreUserGroup(groupId);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowConflict_whenRestoringActiveGroup() {
        userGroup.setIsDeleted(false);

        when(userGroupRepository.findById(groupId)).thenReturn(Optional.of(userGroup));

        assertThatThrownBy(() -> userGroupService.restoreUserGroup(groupId))
                .isInstanceOf(ConflictException.class);
    }
}