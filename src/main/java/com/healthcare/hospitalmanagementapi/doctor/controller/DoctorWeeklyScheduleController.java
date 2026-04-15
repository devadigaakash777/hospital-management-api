package com.healthcare.hospitalmanagementapi.doctor.controller;

import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.CreateDoctorWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.DoctorWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorWeeklyScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/weekly-schedules")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
@Tag(
        name = "Doctor Weekly Schedule Management",
        description = "Operations related to doctor weekly schedule management"
)
public class DoctorWeeklyScheduleController {

    private final DoctorWeeklyScheduleService doctorWeeklyScheduleService;

    @Operation(
            summary = "Create weekly schedules for a doctor",
            description = "Creates one or more weekly schedule entries for the specified doctor"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Weekly schedules created successfully",
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = DoctorWeeklyScheduleResponseDTO.class))
            )
    )
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Duplicate schedule exists", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId) or hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PostMapping
    public ResponseEntity<List<DoctorWeeklyScheduleResponseDTO>> createWeeklySchedules(
            @Parameter(description = "Doctor ID")
            @PathVariable UUID doctorId,
            @RequestBody @Valid List<@Valid CreateDoctorWeeklyScheduleRequestDTO> request
    ) {
        List<DoctorWeeklyScheduleResponseDTO> response =
                doctorWeeklyScheduleService.bulkCreate(doctorId, request);

        URI location = URI.create("/api/v1/doctors/" + doctorId + "/weekly-schedules");

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Get all weekly schedules for a doctor",
            description = "Fetch all weekly schedule entries belonging to the specified doctor"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Weekly schedules fetched successfully",
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = DoctorWeeklyScheduleResponseDTO.class))
            )
    )
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @GetMapping
    public ResponseEntity<List<DoctorWeeklyScheduleResponseDTO>> getAllWeeklySchedules(
            @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(doctorWeeklyScheduleService.getAllByDoctor(doctorId));
    }

    @Operation(
            summary = "Get weekly schedule by ID",
            description = "Fetch a specific weekly schedule entry for the specified doctor"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Weekly schedule fetched successfully",
            content = @Content(schema = @Schema(implementation = DoctorWeeklyScheduleResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Doctor or schedule not found", content = @Content)
    @GetMapping("/{scheduleId}")
    public ResponseEntity<DoctorWeeklyScheduleResponseDTO> getWeeklyScheduleById(
            @PathVariable UUID doctorId,
            @PathVariable UUID scheduleId
    ) {
        return ResponseEntity.ok(doctorWeeklyScheduleService.getById(doctorId, scheduleId));
    }

    @Operation(
            summary = "Update weekly schedules",
            description = "Bulk update one or more weekly schedule entries for the specified doctor"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Weekly schedules updated successfully",
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = DoctorWeeklyScheduleResponseDTO.class))
            )
    )
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor or schedule not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Duplicate schedule exists", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId) or hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @PutMapping
    public ResponseEntity<List<DoctorWeeklyScheduleResponseDTO>> updateWeeklySchedules(
            @PathVariable UUID doctorId,
            @RequestBody @Valid List<@Valid CreateDoctorWeeklyScheduleRequestDTO> request
    ) {
        return ResponseEntity.ok(doctorWeeklyScheduleService.bulkUpdate(doctorId, request));
    }

    @Operation(
            summary = "Delete weekly schedules",
            description = "Delete one or more weekly schedule entries for the specified doctor"
    )
    @ApiResponse(responseCode = "204", description = "Weekly schedules deleted successfully")
    @ApiResponse(responseCode = "404", description = "Doctor or schedule not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId) or hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @DeleteMapping
    public ResponseEntity<Void> deleteWeeklySchedules(
            @PathVariable UUID doctorId,
            @RequestParam List<UUID> scheduleIds
    ) {
        doctorWeeklyScheduleService.bulkDelete(doctorId, scheduleIds);
        return ResponseEntity.noContent().build();
    }
}
