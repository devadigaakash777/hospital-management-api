package com.healthcare.hospitalmanagementapi.doctor.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.CreateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.DoctorBlockedTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.UpdateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorBlockedTimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/blocked-time-slots")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
@Tag(name = "Doctor Blocked Time Slot Management", description = "Operations related to doctor blocked time slot management")
public class DoctorBlockedTimeSlotController {

    private final DoctorBlockedTimeSlotService doctorBlockedTimeSlotService;

    @Operation(summary = "Create blocked time slot for doctor")
    @ApiResponse(
            responseCode = "201",
            description = "The blocked time slot has been successfully created. The Location header contains the URI of the newly created resource.",
            content = @Content(schema = @Schema(implementation = DoctorBlockedTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @ApiResponse(
            responseCode = "409",
            description = """
                The request could not be completed due to a conflict with the current state of the resource:
                - The provided start time must be earlier than the end time.
                - The requested time range overlaps with an existing blocked time slot for this doctor.
                """,
            content = @Content
    )
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId)")
    @PostMapping
    public ResponseEntity<DoctorBlockedTimeSlotResponseDTO> createBlockedTimeSlot(
            @PathVariable UUID doctorId,
            @RequestBody @Valid CreateDoctorBlockedTimeSlotRequestDTO requestDTO
    ) {
        DoctorBlockedTimeSlotResponseDTO response =
                doctorBlockedTimeSlotService.createBlockedTimeSlot(doctorId, requestDTO);

        URI location = URI.create(
                "/api/v1/doctors/" + doctorId + "/blocked-time-slots/" + response.getId()
        );

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get blocked time slot by ID")
    @ApiResponse(
            responseCode = "200",
            description = "The blocked time slot record was retrieved successfully.",
            content = @Content(schema = @Schema(implementation = DoctorBlockedTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "No blocked time slot record was found for the provided identifier.", content = @Content)
    @GetMapping("/{blockedTimeSlotId}")
    public ResponseEntity<DoctorBlockedTimeSlotResponseDTO> getBlockedTimeSlotById(
            @PathVariable UUID blockedTimeSlotId
    ) {
        return ResponseEntity.ok(
                doctorBlockedTimeSlotService.getBlockedTimeSlotById(blockedTimeSlotId)
        );
    }

    @Operation(summary = "Get blocked time slots for doctor")
    @ApiResponse(
            responseCode = "200",
            description = "A paginated list of blocked time slots for the specified doctor was retrieved successfully.",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @GetMapping
    public ResponseEntity<PageResponse<DoctorBlockedTimeSlotResponseDTO>> getBlockedTimeSlotsByDoctor(
            @PathVariable UUID doctorId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorBlockedTimeSlotService.getBlockedTimeSlotsByDoctor(doctorId, pageable)
        );
    }

    @Operation(summary = "Get blocked time slots by date")
    @ApiResponse(
            responseCode = "200",
            description = "A paginated list of blocked time slots for the specified doctor and date was retrieved successfully.",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "The provided date is missing or does not conform to the expected ISO format (yyyy-MM-dd).", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @GetMapping("/date")
    public ResponseEntity<PageResponse<DoctorBlockedTimeSlotResponseDTO>> getBlockedTimeSlotsByDate(
            @PathVariable UUID doctorId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate blockedDate,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorBlockedTimeSlotService.getBlockedTimeSlotsByDate(
                        doctorId,
                        blockedDate,
                        pageable
                )
        );
    }

    @Operation(summary = "Delete blocked time slot")
    @ApiResponse(responseCode = "204", description = "The blocked time slot has been successfully deleted. No content is returned.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "No blocked time slot record was found for the provided identifier.", content = @Content)
    @ApiResponse(
            responseCode = "409",
            description = """
                The request could not be completed due to a conflict with the current state of the resource:
                - Blocked time slots that have already passed cannot be deleted.
                - A blocked time slot may only be deleted prior to its scheduled start time.
                """
    )
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @DeleteMapping("/{blockedTimeSlotId}")
    public ResponseEntity<Void> deleteBlockedTimeSlot(
            @PathVariable UUID blockedTimeSlotId
    ) {
        doctorBlockedTimeSlotService.deleteBlockedTimeSlot(blockedTimeSlotId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update blocked time slot")
    @ApiResponse(
            responseCode = "200",
            description = "The blocked time slot has been successfully updated. The updated resource is returned in the response body.",
            content = @Content(schema = @Schema(implementation = DoctorBlockedTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "No blocked time slot record was found for the provided identifier.", content = @Content)
    @ApiResponse(
            responseCode = "409",
            description = """
                The request could not be completed due to a conflict with the current state of the resource:
                - Blocked time slots that have already passed cannot be modified.
                - The start time may only be updated before the scheduled block begins.
                - The end time must be greater than the current time.
                - The end time must be later than the start time.
                """,
            content = @Content
    )
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId)")
    @PatchMapping("/{blockedTimeSlotId}")
    public ResponseEntity<DoctorBlockedTimeSlotResponseDTO> updateBlockedTimeSlot(
            @PathVariable UUID doctorId,
            @PathVariable UUID blockedTimeSlotId,
            @RequestBody @Valid UpdateDoctorBlockedTimeSlotRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(
                doctorBlockedTimeSlotService.updateBlockedTimeSlot(
                        blockedTimeSlotId,
                        requestDTO
                )
        );
    }
}