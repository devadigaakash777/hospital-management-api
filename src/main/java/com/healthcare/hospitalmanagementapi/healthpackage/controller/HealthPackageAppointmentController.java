package com.healthcare.hospitalmanagementapi.healthpackage.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.CreateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.UpdateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health-package-appointments")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
@Tag(name = "Health Package Appointment Management", description = "Operations related to health package appointment management")
public class HealthPackageAppointmentController {

    private final HealthPackageAppointmentService healthPackageAppointmentService;

    @PostMapping
    @Operation(summary = "Create a new health package appointment")
    @ApiResponse(responseCode = "201", description = "The health package appointment has been successfully created. The Location header contains the URI of the newly created resource.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The referenced health package or time slot could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The requested time slot has reached its maximum capacity, or the booking conditions could not be satisfied for the selected date.", content = @Content)
    public ResponseEntity<HealthPackageAppointmentResponseDTO> createAppointment(
            @RequestBody @Valid CreateHealthPackageAppointmentRequestDTO request
    ) {
        HealthPackageAppointmentResponseDTO response =
                healthPackageAppointmentService.createAppointment(request);
        URI location = URI.create("/api/v1/health-package-appointments/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get a health package appointment by ID")
    @ApiResponse(responseCode = "200", description = "The health package appointment record was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "No health package appointment was found for the provided identifier.", content = @Content)
    public ResponseEntity<HealthPackageAppointmentResponseDTO> getAppointmentById(
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.getAppointmentById(appointmentId));
    }

    @GetMapping
    @Operation(summary = "Get all active health package appointments (paginated)")
    @ApiResponse(responseCode = "200", description = "A paginated list of active health package appointments was retrieved successfully.")
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> getAllAppointments(
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.getAllAppointments(pageable));
    }

    @GetMapping("/including-deleted")
    @Operation(summary = "Get all health package appointments including soft-deleted (paginated)")
    @ApiResponse(responseCode = "200", description = "A paginated list of all health package appointments, including soft-deleted records, was retrieved successfully.", content = @Content)
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> getAllAppointmentsIncludingDeleted(
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.getAllAppointmentsIncludingDeleted(pageable));
    }

    @GetMapping("/by-health-package/{healthPackageId}")
    @Operation(summary = "Get appointments for a specific health package (paginated)")
    @ApiResponse(responseCode = "200", description = "A paginated list of appointments associated with the specified health package was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> getAppointmentsByHealthPackageId(
            @PathVariable UUID healthPackageId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                healthPackageAppointmentService.getAppointmentsByHealthPackageId(healthPackageId, pageable));
    }

    @GetMapping("/by-time-slot/{healthPackageTimeSlotId}")
    @Operation(summary = "Get appointments for a specific time slot (paginated)")
    @ApiResponse(responseCode = "200", description = "A paginated list of appointments associated with the specified time slot was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified time slot could not be found or has been removed.", content = @Content)
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> getAppointmentsByHealthPackageTimeSlotId(
            @PathVariable UUID healthPackageTimeSlotId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                healthPackageAppointmentService.getAppointmentsByHealthPackageTimeSlotId(healthPackageTimeSlotId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter health package appointments (paginated)")
    @ApiResponse(responseCode = "200", description = "A paginated list of health package appointments matching the specified search criteria was retrieved successfully.")
    @ApiResponse(responseCode = "400", description = "The provided search criteria are invalid or contain unsupported filter values.", content = @Content)
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> searchAppointments(
            @Valid @ModelAttribute HealthPackageAppointmentSearchRequestDTO request,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                healthPackageAppointmentService.searchAppointments(request, pageable)
        );
    }

    @PatchMapping("/{appointmentId}")
    @Operation(summary = "Partially update a health package appointment (e.g. change status)")
    @ApiResponse(responseCode = "200", description = "The health package appointment has been successfully updated. The updated resource is returned in the response body.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "404", description = "No health package appointment was found for the provided identifier.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The requested status transition is not permitted for the current appointment state.", content = @Content)
    public ResponseEntity<HealthPackageAppointmentResponseDTO> updateAppointment(
            @PathVariable UUID appointmentId,
            @RequestBody @Valid UpdateHealthPackageAppointmentRequestDTO request
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.updateAppointment(appointmentId, request));
    }

    @DeleteMapping("/{appointmentId}")
    @Operation(summary = "Soft-delete a health package appointment")
    @ApiResponse(responseCode = "204", description = "The health package appointment has been successfully soft-deleted. No content is returned.")
    @ApiResponse(responseCode = "404", description = "No health package appointment was found for the provided identifier.", content = @Content)
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable UUID appointmentId
    ) {
        healthPackageAppointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{appointmentId}/restore")
    @Operation(summary = "Restore a soft-deleted health package appointment")
    @ApiResponse(responseCode = "200", description = "The health package appointment has been successfully restored and is now active. The restored resource is returned in the response body.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "No health package appointment was found for the provided identifier.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The health package appointment is currently active and does not require restoration.", content = @Content)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HealthPackageAppointmentResponseDTO> restoreAppointment(
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.restoreAppointment(appointmentId));
    }
}