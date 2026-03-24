package com.healthcare.hospitalmanagementapi.department.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.dto.*;

import java.util.UUID;

public interface DepartmentService {

    DepartmentResponseDTO createDepartment(CreateDepartmentRequestDTO dto);

    DepartmentResponseDTO getDepartmentById(UUID id);

    PageResponse<DepartmentResponseDTO> getAllDepartments(int page, int size);

    DepartmentResponseDTO updateDepartment(UUID id, UpdateDepartmentRequestDTO dto);

    void deleteDepartment(UUID id);

    public DepartmentResponseDTO restoreDepartment(UUID id);
}