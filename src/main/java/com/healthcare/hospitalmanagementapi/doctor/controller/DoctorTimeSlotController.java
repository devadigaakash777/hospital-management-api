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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/time-slots")
@RequiredArgsConstructor
@Tag(name = "Doctor Time Slot Management", description = "Operations related to doctor time slot management")
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
public class DoctorTimeSlotController {

    private final DoctorTimeSlotService doctorTimeSlotService;

    @Operation(summary = "Create doctor time slot")
    @ApiResponse(
            responseCode = "201",
            description = "Doctor time slot created successfully",
            content = @Content(schema = @Schema(implementation = DoctorTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission", content = @Content)
    @ApiResponse(
            responseCode = "409",
            description = """
                Conflict occurred:
                - Start time must be before end time
                - Reserved slots cannot be greater than total slots
                - Time slot overlaps with an existing time slot
                """
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
            description = "Doctor time slots fetched successfully",
            content = @Content(schema = @Schema(implementation = DoctorTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @GetMapping
    public ResponseEntity<List<DoctorTimeSlotResponseDTO>> getAllTimeSlots(
            @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(doctorTimeSlotService.getAllByDoctor(doctorId));
    }

    @Operation(summary = "Partially update doctor time slot")
    @ApiResponse(
            responseCode = "200",
            description = "Doctor time slot updated successfully",
            content = @Content(schema = @Schema(implementation = DoctorTimeSlotResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor or time slot not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @ApiResponse(
            responseCode = "409",
            description = """
                Conflict occurred:
                - Start time must be before end time
                - Reserved slots cannot be greater than total slots
                - Time slot overlaps with an existing time slot
                """
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
    @ApiResponse(responseCode = "204", description = "Doctor time slot deleted successfully")
    @ApiResponse(responseCode = "404", description = "Doctor or time slot not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId) or  hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteTimeSlot(
            @PathVariable UUID doctorId,
            @PathVariable UUID slotId
    ) {
        doctorTimeSlotService.delete(doctorId, slotId);
        return ResponseEntity.noContent().build();
    }
}
