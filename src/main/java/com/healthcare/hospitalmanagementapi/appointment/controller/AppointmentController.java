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
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
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
            description = "Appointment created successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = """
                    Invalid request:
                    - Validation failure in request body
                    - Appointment date is in the past
                    - Appointment date exceeds advance booking limit
                    - Doctor time slot does not belong to doctor
                    - Doctor is not available for selected day/week
                    - Booking attempted after allowed slot time
                    """,
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = """
                    Resource not found:
                    - Patient not found
                    - Doctor not found
                    - Doctor time slot not found
                    """,
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = """
                    Conflict:
                    - Appointment limit reached for selected time slot
                    - VIP appointment limit reached for selected time slot
                    - Doctor is not available on selected date
                    - Selected doctor time slot is blocked for the selected date
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
            description = "Appointment fetched successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Appointment not found",
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
            description = "Appointments fetched successfully",
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
            description = "Appointments fetched successfully",
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
            description = "Appointments fetched successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Doctor not found",
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
            description = "Appointments fetched successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
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
            description = "Appointments fetched successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid search criteria",
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
            description = "Appointment updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = """
                    Invalid request:
                    - Validation failed
                    - Invalid appointment status transition
                    - Attempted to update an immutable appointment
                    """,
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "Appointment not found",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = """
                    Conflict:
                    - Updated VIP / non-VIP slot capacity exceeded
                    """,
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
            description = "Appointment deleted successfully"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Appointment not found",
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
            description = "Appointment restored successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppointmentResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Appointment not found",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "Appointment is already active",
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
            description = "Appointments fetched successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Doctor time slot not found",
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