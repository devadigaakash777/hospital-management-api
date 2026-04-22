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
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Create a doctor", description = "Creates a new doctor and optionally creates the doctor's weekly schedules")
    @ApiResponse(
            responseCode = "201",
            description = "Doctor created successfully",
            content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "User or department not found",
            content = @Content
    )
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @ApiResponse(responseCode = "409", description = "Doctor already exists or related conflict occurred")
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
    @ApiResponse(responseCode = "200", description = "Doctor fetched successfully",
            content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(
            @Parameter(description = "Doctor ID")
            @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @Operation(summary = "Get all doctors", description = "Fetch all doctors with pagination")
    @ApiResponse(responseCode = "200", description = "Doctors fetched successfully")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Doctors fetched successfully")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Doctors fetched successfully")
    @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Doctors fetched successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "Doctor partially updated successfully",
            content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor or department not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PatchMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> patchDoctor(
            @PathVariable UUID doctorId,
            @RequestBody @Valid UpdateDoctorRequestDTO request
    ) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorId, request));
    }

    @Operation(summary = "Delete doctor", description = "Soft delete a doctor")
    @ApiResponse(responseCode = "204", description = "Doctor deleted successfully")
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @DeleteMapping("/{doctorId}")
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable UUID doctorId
    ) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore doctor", description = "Restore a previously soft deleted doctor")
    @ApiResponse(responseCode = "200", description = "Doctor restored successfully",
            content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Doctor is already active and cannot be restored")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PostMapping("/{doctorId}/restore")
    public ResponseEntity<DoctorResponseDTO> restoreDoctor(
            @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(doctorService.restoreDoctor(doctorId));
    }

    @Operation(summary = "Get doctor availability", description = "Fetch the doctor's weekly schedules and blocked dates using the doctor ID"
    )
    @ApiResponse(responseCode = "200", description = "Doctor availability fetched successfully",
            content = @Content(
                    schema = @Schema(implementation = DoctorAvailabilityResponseDTO.class)
            )
    )
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
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
