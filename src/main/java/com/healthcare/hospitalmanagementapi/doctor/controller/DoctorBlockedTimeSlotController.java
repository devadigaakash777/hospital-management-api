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
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
@Tag(name = "Doctor Blocked Time Slot Management", description = "Operations related to doctor blocked time slot management")
public class DoctorBlockedTimeSlotController {

    private final DoctorBlockedTimeSlotService doctorBlockedTimeSlotService;

    @Operation(summary = "Create blocked time slot for doctor")
    @ApiResponse(
            responseCode = "201",
            description = "Blocked time slot created successfully",
            content = @Content(schema = @Schema(implementation = DoctorBlockedTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @ApiResponse(
            responseCode = "409",
            description = """
                Conflict occurred:
                - Start time must be before end time
                - Blocked time slot overlaps with an existing blocked time slot
                """
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
            description = "Blocked time slot fetched successfully",
            content = @Content(schema = @Schema(implementation = DoctorBlockedTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Blocked time slot not found", content = @Content)
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
            description = "Blocked time slots fetched successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
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
            description = "Blocked time slots fetched successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid date", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
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
    @ApiResponse(responseCode = "204", description = "Blocked time slot deleted successfully")
    @ApiResponse(responseCode = "404", description = "Blocked time slot not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @ApiResponse(
            responseCode = "409",
            description = """
                Conflict occurred:
                - Past blocked time slot cannot be deleted
                - Blocked time slot can only be deleted before the blocked start time
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
            description = "Blocked time slot updated successfully",
            content = @Content(schema = @Schema(implementation = DoctorBlockedTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Blocked time slot not found", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @ApiResponse(
            responseCode = "409",
            description = """
        Conflict occurred:
        - Past blocked time slot cannot be updated
        - Start time can only be updated before the blocked slot start time
        - End time must be greater than current time
        - End time must be after start time
        """
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
