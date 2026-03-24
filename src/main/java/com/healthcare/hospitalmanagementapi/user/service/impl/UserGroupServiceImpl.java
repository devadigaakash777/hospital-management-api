package com.healthcare.hospitalmanagementapi.user.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.user.dto.group.*;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import com.healthcare.hospitalmanagementapi.user.mapper.UserGroupMapper;
import com.healthcare.hospitalmanagementapi.user.repository.UserGroupRepository;
import com.healthcare.hospitalmanagementapi.user.service.UserGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "userGroups")
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final UserGroupMapper userGroupMapper;

    @Override
    @CachePut(key = "#result.id")
    public UserGroupResponseDTO createUserGroup(CreateUserGroupRequestDTO dto) {

        if (userGroupRepository.existsByGroupNameAndIsDeletedFalse(dto.getGroupName())) {
            throw new ConflictException("Group name already exists");
        }

        Set<UUID> uniqueDeptIds;
        List<Department> departments = Collections.emptyList();

        if (dto.getDepartmentIds() != null && !dto.getDepartmentIds().isEmpty()) {

            uniqueDeptIds = new HashSet<>(dto.getDepartmentIds());

            if (uniqueDeptIds.size() != dto.getDepartmentIds().size()) {
                throw new ConflictException("Duplicate department IDs are not allowed");
            }

            departments = departmentRepository.findAllByIdInAndIsDeletedFalse(uniqueDeptIds);

            if (departments.size() != uniqueDeptIds.size()) {
                throw new ResourceNotFoundException("One or more departments not found or deleted");
            }
        }

        UserGroup userGroup = userGroupMapper.toEntity(dto);

        userGroup.setDepartments(new HashSet<>(departments));

        UserGroup saved = userGroupRepository.save(userGroup);

        log.info("UserGroup created with id: {}", saved.getId());

        return userGroupMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public UserGroupResponseDTO getUserGroupById(UUID id) {

        UserGroup userGroup = userGroupRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup not found"));

        return userGroupMapper.toResponseDTO(userGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserGroupResponseDTO> getAllUserGroups(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<UserGroup> pageData = userGroupRepository.findAllByIsDeletedFalse(pageable);

        Page<UserGroupResponseDTO> dtoPage = pageData.map(userGroupMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @CachePut(key = "#id")
    public UserGroupResponseDTO updateUserGroup(UUID id, UpdateUserGroupRequestDTO dto) {

        UserGroup userGroup = userGroupRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup not found"));

        if (dto.getGroupName() != null &&
                !dto.getGroupName().equals(userGroup.getGroupName()) &&
                userGroupRepository.existsByGroupNameAndIsDeletedFalse(dto.getGroupName())) {
            throw new ConflictException("Group name already exists");
        }

        userGroupMapper.updateUserGroupFromDTO(dto, userGroup);

        if (dto.getDepartmentIds() != null) {

            Set<UUID> uniqueDeptIds = new HashSet<>(dto.getDepartmentIds());

            if (uniqueDeptIds.size() != dto.getDepartmentIds().size()) {
                throw new IllegalArgumentException("Duplicate department IDs are not allowed");
            }

            List<Department> departments =
                    departmentRepository.findAllByIdInAndIsDeletedFalse(uniqueDeptIds);

            if (departments.size() != uniqueDeptIds.size()) {
                throw new ResourceNotFoundException("One or more departments not found or deleted");
            }

            userGroup.setDepartments(new HashSet<>(departments));
        }

        UserGroup updated = userGroupRepository.save(userGroup);

        log.info("UserGroup updated with id: {}", id);

        return userGroupMapper.toResponseDTO(updated);
    }

    @Override
    @CacheEvict(key = "#id")
    public void deleteUserGroup(UUID id) {

        UserGroup userGroup = userGroupRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup not found"));

        userGroupRepository.delete(userGroup);

        log.info("UserGroup soft deleted with id: {}", id);
    }

    @Override
    @CachePut(key = "#id")
    public UserGroupResponseDTO restoreUserGroup(UUID id) {

        UserGroup userGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroup not found"));

        if (!Boolean.TRUE.equals(userGroup.getIsDeleted())) {
            throw new ConflictException("UserGroup is already active and cannot be restored");
        }

        userGroup.setIsDeleted(false);
        userGroup.setDeletedAt(null);

        UserGroup saved = userGroupRepository.save(userGroup);

        log.info("UserGroup restored with id: {}", id);

        return userGroupMapper.toResponseDTO(saved);
    }

    private void setDepartments(UserGroup userGroup, Set<UUID> departmentIds) {

        if (departmentIds != null && !departmentIds.isEmpty()) {

            Set<Department> departments = new HashSet<>(departmentRepository
                    .findAllById(departmentIds));

            userGroup.setDepartments(departments);
        }
    }
}