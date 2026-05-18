package com.healthcare.hospitalmanagementapi.doctor.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.*;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Management", description = "Operations related to doctor management")
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Create a doctor", description = "Creates a new doctor and optionally creates the doctor's weekly schedules")
    @ApiResponse(
            responseCode = "201",
            description = "The doctor profile has been successfully created. The Location header contains the URI of the newly created resource.",
            content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "The referenced user or department could not be found or has been removed.",
            content = @Content
    )
    @ApiResponse(responseCode = "409", description = "A doctor profile already exists for the specified user, or another conflicting record was detected.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @RequestBody @Valid CreateDoctorRequestDTO request
    ) {
        DoctorResponseDTO response = doctorService.createDoctor(request);
        URI location = URI.create("/api/v1/doctors/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get doctor by ID", description = "Fetch a doctor using its unique identifier")
    @ApiResponse(responseCode = "200", description = "The doctor profile was retrieved successfully.",
            content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(
            @Parameter(description = "Doctor ID")
            @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @Operation(summary = "Get all doctors", description = "Fetch all doctors with pagination")
    @ApiResponse(responseCode = "200", description = "A paginated list of active doctor profiles was retrieved successfully.")
    @ApiResponse(responseCode = "400", description = "The provided pagination parameters are invalid.", content = @Content)
    @GetMapping
    public ResponseEntity<PageResponse<DoctorResponseDTO>> getAllDoctors(
            @ParameterObject Pageable pageable
    ) {
        PageResponse<DoctorResponseDTO> response = doctorService.getAllDoctors(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all doctors including deleted records", description = "Fetch all doctors including deleted doctors with pagination")
    @ApiResponse(responseCode = "200", description = "A paginated list of all doctor profiles, including soft-deleted records, was retrieved successfully.")
    @ApiResponse(responseCode = "400", description = "The provided pagination parameters are invalid.", content = @Content)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @GetMapping("/all")
    public ResponseEntity<PageResponse<DoctorResponseDTO>> getAllDoctorsIncDel(
            @ParameterObject Pageable pageable
    ) {
        PageResponse<DoctorResponseDTO> response = doctorService.getAllDoctorsIncDel(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get doctors by department", description = "Fetch all doctors belonging to a department")
    @ApiResponse(responseCode = "200", description = "The list of doctors associated with the specified department was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified department could not be found or has been removed.", content = @Content)
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DoctorShortResponseDTO>> getDoctorsByDepartment(
            @PathVariable UUID departmentId
    ) {
        List<DoctorShortResponseDTO> response = doctorService.getDoctorsByDepartment(departmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Search doctors",
            description = "Search doctors by first name, last name, full name, or department name"
    )
    @ApiResponse(responseCode = "200", description = "A paginated list of doctors matching the search criteria was retrieved successfully.")
    @ApiResponse(responseCode = "400", description = "The search request contains invalid or missing parameters.", content = @Content)
    @GetMapping("/search")
    public ResponseEntity<PageResponse<DoctorResponseDTO>> searchDoctors(
            @RequestParam String keyword,
            @ParameterObject Pageable pageable
    ) {
        PageResponse<DoctorResponseDTO> response = doctorService.searchDoctors(
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Partially update doctor", description = "Partially update doctor fields")
    @ApiResponse(responseCode = "200", description = "The doctor profile has been successfully updated. The updated resource is returned in the response body.",
            content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor or department could not be found or has been removed.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PatchMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> patchDoctor(
            @PathVariable UUID doctorId,
            @RequestBody @Valid UpdateDoctorRequestDTO request
    ) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorId, request));
    }

    @Operation(summary = "Delete doctor", description = "Soft delete a doctor")
    @ApiResponse(responseCode = "204", description = "The doctor profile has been successfully soft-deleted. No content is returned.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @DeleteMapping("/{doctorId}")
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable UUID doctorId
    ) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore doctor", description = "Restore a previously soft deleted doctor")
    @ApiResponse(responseCode = "200", description = "The doctor profile has been successfully restored and is now active. The restored resource is returned in the response body.",
            content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The doctor profile is currently active and does not require restoration.", content = @Content)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PostMapping("/{doctorId}/restore")
    public ResponseEntity<DoctorResponseDTO> restoreDoctor(
            @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(doctorService.restoreDoctor(doctorId));
    }

    @Operation(summary = "Get doctor availability", description = "Fetch the doctor's weekly schedules and blocked dates using the doctor ID")
    @ApiResponse(responseCode = "200", description = "The availability details for the specified doctor, including weekly schedules and blocked dates, were retrieved successfully.",
            content = @Content(
                    schema = @Schema(implementation = DoctorAvailabilityResponseDTO.class)
            )
    )
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @GetMapping("/{doctorId}/availability")
    public ResponseEntity<DoctorAvailabilityResponseDTO> getDoctorAvailability(
            @Parameter(
                    description = "Unique identifier of the doctor",
                    required = true
            )
            @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(doctorService.getDoctorAvailability(doctorId));
    }
}