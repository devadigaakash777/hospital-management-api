package com.healthcare.hospitalmanagementapi.department.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.dto.*;
import com.healthcare.hospitalmanagementapi.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "Operations related to department management")
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "Create a new department")
    @ApiResponse(responseCode = "201", description = "Department created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @RequestBody @Valid CreateDepartmentRequestDTO request
    ) {
        DepartmentResponseDTO response = departmentService.createDepartment(request);
        URI location = URI.create("/api/v1/departments/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get department by ID")
    @ApiResponse(responseCode = "200", description = "Department fetched successfully")
    @ApiResponse(responseCode = "404", description = "Department not found")
    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(
            @PathVariable UUID departmentId
    ) {
        return ResponseEntity.ok(departmentService.getDepartmentById(departmentId));
    }

    @Operation(summary = "Get all departments with pagination")
    @ApiResponse(responseCode = "200", description = "Departments fetched successfully")
    @GetMapping
    public ResponseEntity<PageResponse<DepartmentResponseDTO>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(departmentService.getAllDepartments(page, size));
    }

    @Operation(summary = "Update department (partial update)")
    @ApiResponse(responseCode = "200", description = "Department updated successfully")
    @ApiResponse(responseCode = "404", description = "Department not found")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable UUID departmentId,
            @RequestBody @Valid UpdateDepartmentRequestDTO request
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(departmentId, request));
    }

    @Operation(summary = "Delete department (soft delete)")
    @ApiResponse(responseCode = "204", description = "Department deleted successfully")
    @ApiResponse(responseCode = "404", description = "Department not found")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable UUID departmentId
    ) {
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore department by ID")
    @ApiResponse(responseCode = "204", description = "Department restored successfully")
    @ApiResponse(responseCode = "404", description = "Department not found")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{departmentId}/restore")
    public ResponseEntity<DepartmentResponseDTO> restoreDepartment(
            @PathVariable UUID departmentId
    ) {
        return ResponseEntity.ok(departmentService.restoreDepartment(departmentId));
    }
}