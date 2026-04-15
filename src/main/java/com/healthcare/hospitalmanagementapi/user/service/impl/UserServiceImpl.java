package com.healthcare.hospitalmanagementapi.user.service.impl;

import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.exception.custom.BadRequestException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import com.healthcare.hospitalmanagementapi.user.mapper.UserMapper;
import com.healthcare.hospitalmanagementapi.user.repository.UserGroupRepository;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import com.healthcare.hospitalmanagementapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    @Override
    @CachePut(key = "#result.id")
    public UserResponseDTO createUser(CreateUserRequestDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails currentUser = null;

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            currentUser = customUserDetails;
        }

        if (currentUser != null && dto.getRole() == Role.ADMIN && currentUser.getRole() != Role.ADMIN) {
                throw new ConflictException("Only ADMIN can create another ADMIN user");
            }


        if (userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        applyGroupOrUserLogic(user, dto.getDepartmentIds(), dto.getGroupId());

        User saved = userRepository.save(user);

        log.info("User created with id: {}", saved.getId());

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

        Page<UserResponseDTO> dtoPage = userPage.map(userMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @CachePut(key = "#id")
    public UserResponseDTO updateUser(UUID id, UpdateUserRequestDTO dto) {

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));

        if (dto.getEmail() != null &&
                !dto.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmailAndIsDeletedFalse(dto.getEmail())) {
            throw new ConflictException("Email already exists");
        }

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

        userRepository.delete(user);

        log.info("User soft deleted with id: {}", id);
    }

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

        Page<UserResponseDTO> dtoPage = users.map(userMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    private void applyGroupOrUserLogic(User user, Set<UUID> departmentIds, UUID groupId) {

        if (groupId != null) {

            UserGroup group = userGroupRepository.findByIdAndIsDeletedFalse(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

            user.setGroup(group);

            user.setDepartments(new HashSet<>(group.getDepartments()));

            user.setCanManageDoctorSlots(group.getCanManageDoctorSlots());
            user.setCanManageStaff(group.getCanManageStaff());
            user.setCanManageGroups(group.getCanManageGroups());
            user.setCanExportReports(group.getCanExportReports());
            user.setCanManageHealthPackages(group.getCanManageHealthPackages());

        } else {

            user.setGroup(null);

            if (departmentIds != null) {

                if (departmentIds.isEmpty()) {
                    user.getDepartments().clear();
                } else {
                    Set<Department> departments = new HashSet<>(
                            departmentRepository.findAllById(departmentIds)
                    );

                    if (departments.size() != departmentIds.size()) {
                        throw new ResourceNotFoundException("One or more departments not found");
                    }

                    user.setDepartments(departments);
                }
            }
        }
    }
}