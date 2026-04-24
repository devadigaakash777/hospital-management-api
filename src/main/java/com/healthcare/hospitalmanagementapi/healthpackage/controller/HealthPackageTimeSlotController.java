package com.healthcare.hospitalmanagementapi.healthpackage.controller;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.CreateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.HealthPackageTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.UpdateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageTimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
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
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
@Tag(name = "Health Package Time Slot Management", description = "Operations related to health package time slot management")
public class HealthPackageTimeSlotController {

    private final HealthPackageTimeSlotService healthPackageTimeSlotService;

    @PostMapping
    @Operation(summary = "Create a new time slot for a health package")
    @ApiResponse(responseCode = "201", description = "Time slot created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Health package not found")
    @ApiResponse(responseCode = "409", description = "Time slot conflict or overlap")
    @ApiResponse(responseCode = "403", description = "Forbidden")
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
    @ApiResponse(responseCode = "200", description = "Time slots retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Health package not found")
    public ResponseEntity<List<HealthPackageTimeSlotResponseDTO>> getAllByHealthPackage(
            @PathVariable UUID healthPackageId
    ) {
        return ResponseEntity.ok(healthPackageTimeSlotService.getAllByHealthPackage(healthPackageId));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available time slots for a health package on a given date")
    @ApiResponse(responseCode = "200", description = "Available time slots retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid date format")
    @ApiResponse(responseCode = "404", description = "Health package not found")
    @ApiResponse(responseCode = "409", description = "Health package not available for selected date")
    public ResponseEntity<List<HealthPackageTimeSlotResponseDTO>> getAvailableSlots(
            @PathVariable UUID healthPackageId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate
    ) {
        return ResponseEntity.ok(healthPackageTimeSlotService.getAvailableSlots(healthPackageId, appointmentDate));
    }


    @PatchMapping("/{slotId}")
    @Operation(summary = "Partially update a time slot for a health package")
    @ApiResponse(responseCode = "200", description = "Time slot updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Health package or time slot not found")
    @ApiResponse(responseCode = "409", description = "Time slot conflict or overlap")
    @ApiResponse(responseCode = "403", description = "Forbidden")
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
    @ApiResponse(responseCode = "204", description = "Time slot deleted successfully")
    @ApiResponse(responseCode = "404", description = "Health package or time slot not found")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID healthPackageId,
            @PathVariable UUID slotId
    ) {
        healthPackageTimeSlotService.delete(healthPackageId, slotId);
        return ResponseEntity.noContent().build();
    }
}