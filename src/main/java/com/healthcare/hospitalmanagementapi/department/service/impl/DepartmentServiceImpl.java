package com.healthcare.hospitalmanagementapi.department.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.dto.*;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.mapper.DepartmentMapper;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "departments")
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private static final String DEPARTMENT_NOT_FOUND_MESSAGE = "Department not found";

    @Override
    @CachePut(key = "#result.id")
    public DepartmentResponseDTO createDepartment(CreateDepartmentRequestDTO dto) {

        if (departmentRepository.existsByDepartmentNameAndIsDeletedFalse(dto.getDepartmentName())) {
            throw new ConflictException("Department already exists");
        }

        Department department = departmentMapper.toEntity(dto);

        Department saved = departmentRepository.save(department);

        log.info("Department created with id: {}", saved.getId());

        return departmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public DepartmentResponseDTO getDepartmentById(UUID id) {

        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT_NOT_FOUND_MESSAGE));

        return departmentMapper.toResponseDTO(department);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponseDTO> getAllDepartments(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Department> pageData = departmentRepository.findAllByIsDeletedFalse(pageable);

        Page<DepartmentResponseDTO> dtoPage = pageData.map(departmentMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @CachePut(key = "#id")
    public DepartmentResponseDTO updateDepartment(UUID id, UpdateDepartmentRequestDTO dto) {

        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT_NOT_FOUND_MESSAGE));

        if (dto.getDepartmentName() != null &&
                !dto.getDepartmentName().equals(department.getDepartmentName()) &&
                departmentRepository.existsByDepartmentNameAndIsDeletedFalse(dto.getDepartmentName())) {
            throw new ConflictException("Department already exists");
        }

        departmentMapper.updateDepartmentFromDTO(dto, department);

        Department updated = departmentRepository.save(department);

        log.info("Department updated with id: {}", id);

        return departmentMapper.toResponseDTO(updated);
    }

    @Override
    @CacheEvict(key = "#id")
    public void deleteDepartment(UUID id) {

        Department department = departmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT_NOT_FOUND_MESSAGE));

        departmentRepository.delete(department);

        log.info("Department soft deleted with id: {}", id);
    }

    @Override
    @CachePut(key = "#id")
    public DepartmentResponseDTO restoreDepartment(UUID id) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT_NOT_FOUND_MESSAGE));

        if (!Boolean.TRUE.equals(department.getIsDeleted())) {
            throw new ConflictException("Department is already active and cannot be restored");
        }

        department.setIsDeleted(false);
        department.setDeletedAt(null);

        Department saved = departmentRepository.save(department);

        log.info("Department restored with id: {}", id);

        return departmentMapper.toResponseDTO(saved);
    }
}