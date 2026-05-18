package com.healthcare.hospitalmanagementapi.healthpackage.controller;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.CreateHealthPackageWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.HealthPackageWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageWeeklyScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
@Tag(name = "Health Package Weekly Schedule Management", description = "Operations related to health package weekly schedule management")
public class HealthPackageWeeklyScheduleController {

    private final HealthPackageWeeklyScheduleService healthPackageWeeklyScheduleService;

    @GetMapping
    @Operation(summary = "Get all weekly schedules for a health package")
    @ApiResponse(responseCode = "200", description = "The list of weekly schedule entries for the specified health package was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    public ResponseEntity<List<HealthPackageWeeklyScheduleResponseDTO>> getAllByHealthPackage(
            @PathVariable UUID healthPackageId
    ) {
        return ResponseEntity.ok(healthPackageWeeklyScheduleService.getAllByHealthPackage(healthPackageId));
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "Get a specific weekly schedule entry by ID")
    @ApiResponse(responseCode = "200", description = "The weekly schedule entry was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified health package or weekly schedule entry could not be found or has been removed.", content = @Content)
    public ResponseEntity<HealthPackageWeeklyScheduleResponseDTO> getById(
            @PathVariable UUID healthPackageId,
            @PathVariable UUID scheduleId
    ) {
        return ResponseEntity.ok(healthPackageWeeklyScheduleService.getById(healthPackageId, scheduleId));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk-create weekly schedule entries for a health package")
    @ApiResponse(responseCode = "201", description = "One or more weekly schedule entries have been successfully created.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "One or more schedule entries in the request conflict with existing records for the same health package, week number, and day of week.", content = @Content)
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
    @ApiResponse(responseCode = "200", description = "The weekly schedule entries have been successfully replaced. The updated collection is returned in the response body.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The request contains duplicate schedule entries for the same week number and day of week combination.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<List<HealthPackageWeeklyScheduleResponseDTO>> bulkUpdate(
            @PathVariable UUID healthPackageId,
            @RequestBody @Valid List<CreateHealthPackageWeeklyScheduleRequestDTO> request
    ) {
        return ResponseEntity.ok(healthPackageWeeklyScheduleService.bulkUpdate(healthPackageId, request));
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Bulk-delete weekly schedule entries for a health package")
    @ApiResponse(responseCode = "204", description = "The specified weekly schedule entries have been successfully deleted. No content is returned.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package or one or more schedule entries could not be found or have been removed.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<Void> bulkDelete(
            @PathVariable UUID healthPackageId,
            @RequestBody List<UUID> scheduleIds
    ) {
        healthPackageWeeklyScheduleService.bulkDelete(healthPackageId, scheduleIds);
        return ResponseEntity.noContent().build();
    }
}