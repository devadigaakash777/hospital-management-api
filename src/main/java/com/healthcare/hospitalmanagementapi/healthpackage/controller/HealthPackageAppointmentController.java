package com.healthcare.hospitalmanagementapi.healthpackage.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.CreateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.UpdateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
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
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
@Tag(name = "Health Package Appointment Management", description = "Operations related to health package appointment management")
public class HealthPackageAppointmentController {

    private final HealthPackageAppointmentService healthPackageAppointmentService;

    @PostMapping
    @Operation(summary = "Create a new health package appointment")
    @ApiResponse(responseCode = "201", description = "Appointment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Health package or time slot not found")
    @ApiResponse(responseCode = "409", description = "Slot full or invalid booking conditions")
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
    @ApiResponse(responseCode = "200", description = "Appointment found")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    public ResponseEntity<HealthPackageAppointmentResponseDTO> getAppointmentById(
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.getAppointmentById(appointmentId));
    }

    @GetMapping
    @Operation(summary = "Get all active health package appointments (paginated)")
    @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully")
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> getAllAppointments(
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.getAllAppointments(pageable));
    }

    @GetMapping("/including-deleted")
    @Operation(summary = "Get all health package appointments including soft-deleted (paginated)")
    @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully")
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> getAllAppointmentsIncludingDeleted(
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.getAllAppointmentsIncludingDeleted(pageable));
    }

    @GetMapping("/by-health-package/{healthPackageId}")
    @Operation(summary = "Get appointments for a specific health package (paginated)")
    @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Health package not found")
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> getAppointmentsByHealthPackageId(
            @PathVariable UUID healthPackageId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                healthPackageAppointmentService.getAppointmentsByHealthPackageId(healthPackageId, pageable));
    }

    @GetMapping("/by-time-slot/{healthPackageTimeSlotId}")
    @Operation(summary = "Get appointments for a specific time slot (paginated)")
    @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Time slot not found")
    public ResponseEntity<PageResponse<HealthPackageAppointmentResponseDTO>> getAppointmentsByHealthPackageTimeSlotId(
            @PathVariable UUID healthPackageTimeSlotId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                healthPackageAppointmentService.getAppointmentsByHealthPackageTimeSlotId(healthPackageTimeSlotId, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter health package appointments (paginated)")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid search criteria")
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
    @ApiResponse(responseCode = "200", description = "Appointment updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    @ApiResponse(responseCode = "409", description = "Invalid status transition")
    public ResponseEntity<HealthPackageAppointmentResponseDTO> updateAppointment(
            @PathVariable UUID appointmentId,
            @RequestBody @Valid UpdateHealthPackageAppointmentRequestDTO request
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.updateAppointment(appointmentId, request));
    }

    @DeleteMapping("/{appointmentId}")
    @Operation(summary = "Soft-delete a health package appointment")
    @ApiResponse(responseCode = "204", description = "Appointment deleted successfully")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable UUID appointmentId
    ) {
        healthPackageAppointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{appointmentId}/restore")
    @Operation(summary = "Restore a soft-deleted health package appointment")
    @ApiResponse(responseCode = "200", description = "Appointment restored successfully")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    @ApiResponse(responseCode = "409", description = "Appointment already active")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HealthPackageAppointmentResponseDTO> restoreAppointment(
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(healthPackageAppointmentService.restoreAppointment(appointmentId));
    }
}