package com.healthcare.hospitalmanagementapi.healthpackage.controller;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.CreateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.HealthPackageTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.UpdateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageTimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health-packages/{healthPackageId}/time-slots")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
@Tag(name = "Health Package Time Slot Management", description = "Operations related to health package time slot management")
public class HealthPackageTimeSlotController {

    private final HealthPackageTimeSlotService healthPackageTimeSlotService;

    @PostMapping
    @Operation(summary = "Create a new time slot for a health package")
    @ApiResponse(responseCode = "201", description = "The time slot has been successfully created. The Location header contains the URI of the newly created resource.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    @ApiResponse(
            responseCode = "409",
            description = """
                The request could not be completed due to a conflict with the current state of the resource:
                - The start time must be earlier than the end time.
                - The requested time range overlaps with an existing time slot for this health package.
                """,
            content = @Content
    )
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<HealthPackageTimeSlotResponseDTO> create(
            @PathVariable UUID healthPackageId,
            @RequestBody @Valid CreateHealthPackageTimeSlotRequestDTO request
    ) {
        HealthPackageTimeSlotResponseDTO response =
                healthPackageTimeSlotService.create(healthPackageId, request);
        URI location = URI.create("/api/v1/health-packages/" + healthPackageId + "/time-slots/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all time slots for a health package")
    @ApiResponse(responseCode = "200", description = "The list of time slots for the specified health package was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    public ResponseEntity<List<HealthPackageTimeSlotResponseDTO>> getAllByHealthPackage(
            @PathVariable UUID healthPackageId
    ) {
        return ResponseEntity.ok(healthPackageTimeSlotService.getAllByHealthPackage(healthPackageId));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available time slots for a health package on a given date")
    @ApiResponse(responseCode = "200", description = "The list of available time slots for the specified health package and appointment date was retrieved successfully.")
    @ApiResponse(responseCode = "400", description = "The provided date is missing or does not conform to the expected ISO format (yyyy-MM-dd).", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The health package is not available on the requested appointment date based on its weekly schedule or advance booking configuration.", content = @Content)
    public ResponseEntity<List<HealthPackageTimeSlotResponseDTO>> getAvailableSlots(
            @PathVariable UUID healthPackageId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate
    ) {
        return ResponseEntity.ok(healthPackageTimeSlotService.getAvailableSlots(healthPackageId, appointmentDate));
    }

    @PatchMapping("/{slotId}")
    @Operation(summary = "Partially update a time slot for a health package")
    @ApiResponse(responseCode = "200", description = "The time slot has been successfully updated. The updated resource is returned in the response body.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package or time slot could not be found or has been removed.", content = @Content)
    @ApiResponse(
            responseCode = "409",
            description = """
                The request could not be completed due to a conflict with the current state of the resource:
                - The start time must be earlier than the end time.
                - The requested time range overlaps with an existing time slot for this health package.
                """,
            content = @Content
    )
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<HealthPackageTimeSlotResponseDTO> update(
            @PathVariable UUID healthPackageId,
            @PathVariable UUID slotId,
            @RequestBody @Valid UpdateHealthPackageTimeSlotRequestDTO request
    ) {
        return ResponseEntity.ok(healthPackageTimeSlotService.update(healthPackageId, slotId, request));
    }

    @DeleteMapping("/{slotId}")
    @Operation(summary = "Delete a time slot for a health package")
    @ApiResponse(responseCode = "204", description = "The time slot has been successfully deleted. No content is returned.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package or time slot could not be found or has been removed.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID healthPackageId,
            @PathVariable UUID slotId
    ) {
        healthPackageTimeSlotService.delete(healthPackageId, slotId);
        return ResponseEntity.noContent().build();
    }
}