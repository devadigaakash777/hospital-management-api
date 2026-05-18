package com.healthcare.hospitalmanagementapi.doctor.controller;

import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.CreateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.DoctorTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.UpdateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorTimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/time-slots")
@RequiredArgsConstructor
@Tag(name = "Doctor Time Slot Management", description = "Operations related to doctor time slot management")
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
public class DoctorTimeSlotController {

    private final DoctorTimeSlotService doctorTimeSlotService;

    @Operation(summary = "Create doctor time slot")
    @ApiResponse(
            responseCode = "201",
            description = "The time slot has been successfully created. The Location header contains the URI of the newly created resource.",
            content = @Content(schema = @Schema(implementation = DoctorTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @ApiResponse(
            responseCode = "409",
            description = """
                The request could not be completed due to a conflict with the current state of the resource:
                - The start time must be earlier than the end time.
                - The number of reserved slots must not exceed the total slot capacity.
                - The requested time range overlaps with an existing time slot for this doctor.
                """,
            content = @Content
    )
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId) or hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PostMapping
    public ResponseEntity<DoctorTimeSlotResponseDTO> createTimeSlot(
            @PathVariable UUID doctorId,
            @RequestBody @Valid CreateDoctorTimeSlotRequestDTO requestDTO
    ) {
        DoctorTimeSlotResponseDTO response = doctorTimeSlotService.create(doctorId, requestDTO);

        URI location = URI.create(
                "/api/v1/doctors/" + doctorId + "/time-slots/" + response.getId()
        );

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get all doctor time slots")
    @ApiResponse(
            responseCode = "200",
            description = "The list of time slots for the specified doctor was retrieved successfully.",
            content = @Content(schema = @Schema(implementation = DoctorTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @GetMapping
    public ResponseEntity<List<DoctorTimeSlotResponseDTO>> getAllTimeSlots(
            @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(doctorTimeSlotService.getAllByDoctor(doctorId));
    }

    @Operation(summary = "Partially update doctor time slot")
    @ApiResponse(
            responseCode = "200",
            description = "The time slot has been successfully updated. The updated resource is returned in the response body.",
            content = @Content(schema = @Schema(implementation = DoctorTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor or time slot could not be found or has been removed.", content = @Content)
    @ApiResponse(
            responseCode = "409",
            description = """
                The request could not be completed due to a conflict with the current state of the resource:
                - The start time must be earlier than the end time.
                - The number of reserved slots must not exceed the total slot capacity.
                - The requested time range overlaps with an existing time slot for this doctor.
                """,
            content = @Content
    )
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId) or hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PatchMapping("/{slotId}")
    public ResponseEntity<DoctorTimeSlotResponseDTO> updateTimeSlot(
            @PathVariable UUID doctorId,
            @PathVariable UUID slotId,
            @RequestBody @Valid UpdateDoctorTimeSlotRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(
                doctorTimeSlotService.update(doctorId, slotId, requestDTO)
        );
    }

    @Operation(summary = "Delete doctor time slot")
    @ApiResponse(responseCode = "204", description = "The time slot has been successfully deleted. No content is returned.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor or time slot could not be found or has been removed.", content = @Content)
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId) or  hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteTimeSlot(
            @PathVariable UUID doctorId,
            @PathVariable UUID slotId
    ) {
        doctorTimeSlotService.delete(doctorId, slotId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get available doctor time slots",
            description = """
                Returns all doctor time slots available for the selected appointment date.

                Availability is calculated by:
                - Doctor weekly schedule
                - Blocked dates
                - Blocked time slots
                - Slot capacity
                - VIP slot capacity when isVip=true
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "The list of available time slots for the specified doctor and appointment date was retrieved successfully.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DoctorTimeSlotResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = """
                The request could not be processed due to invalid input:
                - The appointment date is missing or does not conform to the expected ISO format (yyyy-MM-dd).
                - The appointment date exceeds the doctor's advance booking limit.
                """,
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "The doctor is not available on the requested appointment date based on their weekly schedule or blocked date configuration.",
            content = @Content
    )
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @GetMapping("/available")
    public ResponseEntity<List<DoctorTimeSlotResponseDTO>> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
            @RequestParam(defaultValue = "false") boolean isVip
    ) {
        return ResponseEntity.ok(
                doctorTimeSlotService.getAvailableSlots(
                        doctorId,
                        appointmentDate,
                        isVip
                )
        );
    }
}