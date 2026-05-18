package com.healthcare.hospitalmanagementapi.healthpackage.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.CreateHealthPackageRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageAvailabilityResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageShortResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.UpdateHealthPackageRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health-packages")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
@Tag(name = "Health Package Management", description = "Operations related to health package management")
public class HealthPackageController {

    private final HealthPackageService healthPackageService;

    @PostMapping
    @Operation(summary = "Create a new health package")
    @ApiResponse(responseCode = "201", description = "The health package has been successfully created. The Location header contains the URI of the newly created resource.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<HealthPackageResponseDTO> createHealthPackage(
            @RequestBody @Valid CreateHealthPackageRequestDTO request
    ) {
        HealthPackageResponseDTO response = healthPackageService.createHealthPackage(request);
        URI location = URI.create("/api/v1/health-packages/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{healthPackageId}")
    @Operation(summary = "Get a health package by ID")
    @ApiResponse(responseCode = "200", description = "The health package was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    public ResponseEntity<HealthPackageResponseDTO> getHealthPackageById(
            @PathVariable UUID healthPackageId
    ) {
        return ResponseEntity.ok(healthPackageService.getHealthPackageById(healthPackageId));
    }

    @GetMapping
    @Operation(summary = "Get all active health packages (paginated)")
    @ApiResponse(responseCode = "200", description = "A paginated list of active health packages was retrieved successfully.", content = @Content)
    public ResponseEntity<PageResponse<HealthPackageResponseDTO>> getAllHealthPackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(healthPackageService.getAllHealthPackages(page, size));
    }

    @GetMapping("/including-deleted")
    @Operation(summary = "Get all health packages including soft-deleted (paginated)")
    @ApiResponse(responseCode = "200", description = "A paginated list of all health packages, including soft-deleted records, was retrieved successfully.")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<PageResponse<HealthPackageResponseDTO>> getAllHealthPackagesIncludingDeleted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(healthPackageService.getAllHealthPackagesIncludingDeleted(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Search health packages by keyword")
    @ApiResponse(responseCode = "200", description = "A paginated list of health packages matching the provided keyword was retrieved successfully.")
    public ResponseEntity<PageResponse<HealthPackageShortResponseDTO>> searchHealthPackages(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(healthPackageService.searchHealthPackages(keyword, page, size));
    }

    @GetMapping("/{healthPackageId}/availability")
    @Operation(summary = "Get availability information for a health package")
    @ApiResponse(responseCode = "200", description = "The availability details for the specified health package, including weekly schedules, were retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    public ResponseEntity<HealthPackageAvailabilityResponseDTO> getHealthPackageAvailability(
            @PathVariable UUID healthPackageId
    ) {
        return ResponseEntity.ok(healthPackageService.getHealthPackageAvailability(healthPackageId));
    }

    @PatchMapping("/{healthPackageId}")
    @Operation(summary = "Partially update a health package")
    @ApiResponse(responseCode = "200", description = "The health package has been successfully updated. The updated resource is returned in the response body.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The update could not be applied due to a conflict with existing business rules or data constraints.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<HealthPackageResponseDTO> updateHealthPackage(
            @PathVariable UUID healthPackageId,
            @RequestBody @Valid UpdateHealthPackageRequestDTO request
    ) {
        return ResponseEntity.ok(healthPackageService.updateHealthPackage(healthPackageId, request));
    }

    @DeleteMapping("/{healthPackageId}")
    @Operation(summary = "Soft-delete a health package")
    @ApiResponse(responseCode = "204", description = "The health package has been successfully soft-deleted. No content is returned.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found or has been removed.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<Void> deleteHealthPackage(
            @PathVariable UUID healthPackageId
    ) {
        healthPackageService.deleteHealthPackage(healthPackageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{healthPackageId}/restore")
    @Operation(summary = "Restore a soft-deleted health package")
    @ApiResponse(responseCode = "200", description = "The health package has been successfully restored and is now active. The restored resource is returned in the response body.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified health package could not be found.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The health package is currently active and does not require restoration.", content = @Content)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_HEALTH_PACKAGES')")
    public ResponseEntity<HealthPackageResponseDTO> restoreHealthPackage(
            @PathVariable UUID healthPackageId
    ) {
        return ResponseEntity.ok(healthPackageService.restoreHealthPackage(healthPackageId));
    }
}