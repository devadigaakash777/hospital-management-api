package com.healthcare.hospitalmanagementapi.security.controller;

import com.healthcare.hospitalmanagementapi.common.exception.dto.ErrorResponse;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogResponseDTO;
import com.healthcare.hospitalmanagementapi.security.service.SuspiciousActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/security")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Security Monitoring", description = "Admin endpoints for investigating suspicious authentication activity")
@SecurityRequirement(name = "bearerAuth")
public class SuspiciousActivityController {

    private final SuspiciousActivityService suspiciousActivityService;

    @Operation(
            summary     = "List suspicious activity logs",
            description = "Returns a paginated list of suspicious authentication events. " +
                    "Supports optional filtering by email address, client IP, and event type. " +
                    "Results are always ordered by most recent first. Requires the ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description  = "Successfully retrieved the page of suspicious activity logs.",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = SuspiciousActivityLogResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description  = "Invalid pagination parameters — page must be ≥ 0, size must be between 1 and 100.",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description  = "Missing or invalid Bearer token.",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description  = "Authenticated user does not have the ADMIN role.",
                    content      = @Content(
                            mediaType = "application/json",
                            schema    = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/suspicious-activity")
    public ResponseEntity<PageResponse<SuspiciousActivityLogResponseDTO>> getLogs(
            @Parameter(
                    in          = ParameterIn.QUERY,
                    description = "Filter by the exact email address of the targeted account. Omit to return all emails.",
                    example     = "john.doe@example.com"
            )
            @RequestParam(required = false) String email,

            @Parameter(
                    in          = ParameterIn.QUERY,
                    description = "Filter by the exact client IP address. Omit to return all IPs.",
                    example     = "192.168.1.100"
            )
            @RequestParam(required = false) String ipAddress,

            @Parameter(
                    in          = ParameterIn.QUERY,
                    description = "Filter by event category. Omit to return all event types.",
                    schema      = @Schema(implementation = SuspiciousEvent.class),
                    example     = "BRUTE_FORCE_DETECTED"
            )
            @RequestParam(required = false) SuspiciousEvent eventType,

            @Parameter(
                    in          = ParameterIn.QUERY,
                    description = "Zero-based page index.",
                    example     = "0"
            )
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(
                    in          = ParameterIn.QUERY,
                    description = "Number of records per page. Must be between 1 and 100.",
                    example     = "20"
            )
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Page<SuspiciousActivityLogResponseDTO> result = suspiciousActivityService.getLogs(
                email,
                ipAddress,
                eventType,
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(PageResponse.of(result));
    }
}