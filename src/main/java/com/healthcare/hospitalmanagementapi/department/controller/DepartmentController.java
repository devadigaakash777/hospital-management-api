package com.healthcare.hospitalmanagementapi.department.controller;

import com.healthcare.hospitalmanagementapi.common.exception.dto.ErrorResponse;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.dto.*;
import com.healthcare.hospitalmanagementapi.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@ApiResponse(
        responseCode = "401",
        description = "Authentication required — no valid credentials were provided. Ensure a valid Bearer token is included in the Authorization header.",
        content = @Content
)
@ApiResponse(
        responseCode = "429",
        description = "Too many requests. Auth endpoints are limited to 100 requests per minute per IP.",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
        )
)
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "Create a new department")
    @ApiResponse(
            responseCode = "201",
            description = "Department created successfully. The Location header contains the URI of the newly created resource.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DepartmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body is invalid or one or more required fields failed validation.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied — only users with the ADMIN role are permitted to create departments.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "A department with the same name already exists and is currently active.",
            content = @Content
    )
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
    @ApiResponse(
            responseCode = "200",
            description = "Department retrieved successfully.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DepartmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active department was found with the provided identifier.",
            content = @Content
    )
    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(
            @PathVariable UUID departmentId
    ) {
        return ResponseEntity.ok(departmentService.getDepartmentById(departmentId));
    }

    @Operation(summary = "Get all departments with pagination")
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of active departments retrieved successfully, ordered by creation date descending.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @GetMapping
    public ResponseEntity<PageResponse<DepartmentResponseDTO>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(departmentService.getAllDepartments(page, size));
    }

    @Operation(summary = "Update department (partial update)")
    @ApiResponse(
            responseCode = "200",
            description = "Department updated successfully. Returns the full updated department record.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DepartmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body is invalid or one or more fields failed validation.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied — only users with the ADMIN role are permitted to update departments.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active department was found with the provided identifier.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "A department with the requested name already exists and is currently active.",
            content = @Content
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable UUID departmentId,
            @RequestBody @Valid UpdateDepartmentRequestDTO request
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(departmentId, request));
    }

    @Operation(summary = "Search departments")
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of departments matching the provided keyword retrieved successfully. Returns an empty page if no records match.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The search keyword or pagination parameters are invalid.",
            content = @Content
    )
    @GetMapping("/search")
    public ResponseEntity<PageResponse<DepartmentResponseDTO>> searchDepartments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(departmentService.searchDepartments(keyword, page, size));
    }

    @Operation(summary = "Delete department (soft delete)")
    @ApiResponse(
            responseCode = "204",
            description = "Department soft-deleted successfully. The record is retained in the system and can be restored if required."
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied — only users with the ADMIN role are permitted to delete departments.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active department was found with the provided identifier.",
            content = @Content
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable UUID departmentId
    ) {
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore department by ID")
    @ApiResponse(
            responseCode = "200",
            description = "Department restored successfully. Returns the full department record in its reinstated active state.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DepartmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied — only users with the ADMIN role are permitted to restore departments.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No department was found with the provided identifier.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "The department is already in an active state and cannot be restored again.",
            content = @Content
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{departmentId}/restore")
    public ResponseEntity<DepartmentResponseDTO> restoreDepartment(
            @PathVariable UUID departmentId
    ) {
        return ResponseEntity.ok(departmentService.restoreDepartment(departmentId));
    }
}