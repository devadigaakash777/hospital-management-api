package com.healthcare.hospitalmanagementapi.appointment.controller;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.CreateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.UpdateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.service.AppointmentService;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
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
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(
        name = "Appointment Management",
        description = "Operations related to appointment management"
)
@ApiResponse(
        responseCode = "401",
        description = "Authentication required — no valid credentials were provided. Ensure a valid Bearer token is included in the Authorization header.",
        content = @Content
)
@PreAuthorize("hasRole('ADMIN') or hasAuthority('CAN_MANAGE_APPOINTMENTS')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(
            summary = "Create appointment",
            description = """
                    Creates a new appointment for a patient with a doctor and time slot.
                    
                    Validation performed:
                    - Patient, doctor, and time slot must exist
                    - Selected time slot must belong to the doctor
                    - Appointment date must not be in the past
                    - Appointment date must be within doctor's advance booking limit
                    - Doctor must be available according to weekly schedule
                    - Appointment date must not be blocked
                    - Selected time slot must not be blocked
                    - Slot capacity / VIP slot capacity must not be exceeded
                    - Same-day appointments can only be booked within 15 minutes of slot start time
                    """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Appointment created successfully. Returns the full appointment record including the assigned token number, status, and associated patient and doctor details.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = """
                    The request could not be processed due to one of the following reasons:
                    - One or more request fields failed validation
                    - The appointment date falls in the past
                    - The appointment date exceeds the doctor's maximum advance booking window
                    - The specified time slot does not belong to the given doctor
                    - The doctor is unavailable on the selected day or within the selected week
                    - The booking attempt was made after the permitted window for same-day slots
                    """,
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = """
                    The request references one or more resources that could not be found:
                    - No patient exists with the provided patient ID
                    - No doctor exists with the provided doctor ID
                    - No time slot exists with the provided time slot ID
                    """,
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = """
                    The request conflicts with the current state of the resource:
                    - The regular appointment capacity for the selected time slot has been reached
                    - The VIP appointment capacity for the selected time slot has been reached
                    - The doctor is marked as unavailable on the selected date
                    - The selected time slot is blocked for the selected date
                    """,
            content = @Content
    )
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @RequestBody @Valid CreateAppointmentRequestDTO request
    ) {

        AppointmentResponseDTO response = appointmentService.createAppointment(request);

        URI location = URI.create("/api/v1/appointments/" + response.getId());

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Get appointment by ID",
            description = "Fetches a single active appointment by its identifier."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Appointment retrieved successfully. Returns the full appointment record including patient, doctor, time slot, and current status details.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active appointment was found with the provided identifier. The appointment may have been deleted or may never have existed.",
            content = @Content
    )
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentById(appointmentId)
        );
    }

    @Operation(
            summary = "Get all active appointments",
            description = """
                    Returns a paginated list of all active appointments.
                    
                    Results are ordered by:
                    1. VIP appointments first
                    2. Token number ascending
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of active appointments retrieved successfully. Results are sorted with VIP appointments first, followed by ascending token number.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @GetMapping
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getAllAppointments(
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAllAppointments(pageable)
        );
    }

    @Operation(
            summary = "Get all appointments including deleted",
            description = """
                    Returns a paginated list of all appointments, including soft-deleted appointments.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of all appointments retrieved successfully, including those that have been soft-deleted. Deleted appointments will have their deletion metadata populated.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @GetMapping("/all")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getAllAppointmentsIncludingDeleted(
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAllAppointmentsIncludingDeleted(pageable)
        );
    }

    @Operation(
            summary = "Get appointments by doctor ID",
            description = "Returns a paginated list of appointments for the specified doctor."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of appointments for the specified doctor retrieved successfully.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No doctor was found with the provided identifier.",
            content = @Content
    )
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getAppointmentsByDoctorId(
            @PathVariable UUID doctorId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctorId(doctorId, pageable)
        );
    }

    @Operation(
            summary = "Get appointments created by user",
            description = "Returns a paginated list of appointments created by the specified user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of appointments created by the specified user retrieved successfully.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No user was found with the provided identifier.",
            content = @Content
    )
    @GetMapping("/created-by/{createdByUserId}")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getAppointmentsByCreatedUserId(
            @PathVariable UUID createdByUserId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByCreatedUserId(createdByUserId, pageable)
        );
    }

    @Operation(
            summary = "Search appointments",
            description = """
                    Searches appointments using one or more optional filters.
                    
                    Supported filters:
                    - doctorId
                    - createdByUserId
                    - appointmentStatus
                    - appointmentDate
                    - isVip
                    - department
                    - search
                    - excludeCancelled
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of appointments matching the specified filter criteria retrieved successfully. Returns an empty page if no records match.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "One or more search filter parameters are invalid or malformed.",
            content = @Content
    )
    @GetMapping("/search")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> searchAppointments(
            @Valid @ModelAttribute AppointmentSearchRequestDTO request,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.searchAppointments(request, pageable)
        );
    }

    @Operation(
            summary = "Update appointment",
            description = """
                    Updates an existing appointment.
                    
                    Allowed status transitions:
                    - CONFIRMED -> CANCELLED, ADMITTED, COMPLETED
                    - ADMITTED -> CANCELLED, COMPLETED
                    
                    Appointments already marked as CANCELLED or COMPLETED cannot be updated.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Appointment updated successfully. Returns the full updated appointment record reflecting the latest state.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = """
                    The request could not be processed due to one of the following reasons:
                    - One or more request fields failed validation
                    - The requested status transition is not permitted (see allowed transitions in the operation description)
                    - The appointment is in a terminal state (CANCELLED or COMPLETED) and cannot be modified
                    """,
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active appointment was found with the provided identifier.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "The updated slot configuration exceeds the permitted capacity for either regular or VIP appointments on the target time slot.",
            content = @Content
    )
    @PatchMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(
            @PathVariable UUID appointmentId,
            @RequestBody @Valid UpdateAppointmentRequestDTO request
    ) {
        return ResponseEntity.ok(
                appointmentService.updateAppointment(appointmentId, request)
        );
    }

    @Operation(
            summary = "Delete appointment",
            description = "Soft deletes an appointment."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Appointment soft-deleted successfully. The record is retained in the system and can be restored if required."
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active appointment was found with the provided identifier.",
            content = @Content
    )
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable UUID appointmentId
    ) {
        appointmentService.deleteAppointment(appointmentId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Restore deleted appointment",
            description = "Restores a previously soft-deleted appointment."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Appointment restored successfully. Returns the full appointment record in its reinstated active state.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No appointment was found with the provided identifier.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "The appointment is already in an active state and cannot be restored again.",
            content = @Content
    )
    @PostMapping("/{appointmentId}/restore")
    public ResponseEntity<AppointmentResponseDTO> restoreAppointment(
            @Parameter(
                    description = "Appointment identifier",
                    required = true
            )
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.restoreAppointment(appointmentId)
        );
    }

    @Operation(
            summary = "Get appointments by doctor time slot ID",
            description = """
                    Returns a paginated list of appointments for the specified doctor time slot.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of appointments for the specified time slot retrieved successfully.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No doctor time slot was found with the provided identifier.",
            content = @Content
    )
    @GetMapping("/time-slot/{doctorTimeSlotId}")
    public ResponseEntity<PageResponse<AppointmentResponseDTO>> getAppointmentsByDoctorTimeSlotId(
            @PathVariable UUID doctorTimeSlotId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctorTimeSlotId(
                        doctorTimeSlotId,
                        pageable
                )
        );
    }
}