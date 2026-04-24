package com.healthcare.hospitalmanagementapi.healthpackage.controller;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.CreateHealthPackageWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.HealthPackageWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageWeeklyScheduleService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health-packages/{healthPackageId}/weekly-schedules")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
@Tag(name = "Health Package Weekly Schedule Management", description = "Operations related to health package weekly schedule management")
public class HealthPackageWeeklyScheduleController {

    private final HealthPackageWeeklyScheduleService healthPackageWeeklyScheduleService;

    @GetMapping
    @Operation(summary = "Get all weekly schedules for a health package")
    @ApiResponse(responseCode = "200", description = "Weekly schedules retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Health package not found")
    public ResponseEntity<List<HealthPackageWeeklyScheduleResponseDTO>> getAllByHealthPackage(
            @PathVariable UUID healthPackageId
    ) {
        return ResponseEntity.ok(healthPackageWeeklyScheduleService.getAllByHealthPackage(healthPackageId));
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "Get a specific weekly schedule entry by ID")
    @ApiResponse(responseCode = "200", description = "Weekly schedule entry found")
    @ApiResponse(responseCode = "404", description = "Health package or schedule not found")
    public ResponseEntity<HealthPackageWeeklyScheduleResponseDTO> getById(
            @PathVariable UUID healthPackageId,
            @PathVariable UUID scheduleId
    ) {
        return ResponseEntity.ok(healthPackageWeeklyScheduleService.getById(healthPackageId, scheduleId));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk-create weekly schedule entries for a health package")
    @ApiResponse(responseCode = "201", description = "Weekly schedules created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Health package not found")
    @ApiResponse(responseCode = "409", description = "Duplicate or conflicting schedule")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<List<HealthPackageWeeklyScheduleResponseDTO>> bulkCreate(
            @PathVariable UUID healthPackageId,
            @RequestBody @Valid List<CreateHealthPackageWeeklyScheduleRequestDTO> request
    ) {
        List<HealthPackageWeeklyScheduleResponseDTO> response =
                healthPackageWeeklyScheduleService.bulkCreate(healthPackageId, request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/bulk")
    @Operation(summary = "Bulk-replace weekly schedule entries for a health package")
    @ApiResponse(responseCode = "200", description = "Weekly schedules updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Health package not found")
    @ApiResponse(responseCode = "409", description = "Duplicate schedule in request")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<List<HealthPackageWeeklyScheduleResponseDTO>> bulkUpdate(
            @PathVariable UUID healthPackageId,
            @RequestBody @Valid List<CreateHealthPackageWeeklyScheduleRequestDTO> request
    ) {
        return ResponseEntity.ok(healthPackageWeeklyScheduleService.bulkUpdate(healthPackageId, request));
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Bulk-delete weekly schedule entries for a health package")
    @ApiResponse(responseCode = "204", description = "Weekly schedules deleted successfully")
    @ApiResponse(responseCode = "404", description = "Health package not found")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<Void> bulkDelete(
            @PathVariable UUID healthPackageId,
            @RequestBody List<UUID> scheduleIds
    ) {
        healthPackageWeeklyScheduleService.bulkDelete(healthPackageId, scheduleIds);
        return ResponseEntity.noContent().build();
    }
}