package com.healthcare.hospitalmanagementapi.doctor.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.CreateDoctorBlockedDateRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.DoctorBlockedDateResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorBlockedDateService;
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
@RequestMapping("/api/v1/doctors/{doctorId}/blocked-dates")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
@Tag(name = "Doctor Blocked Date Management", description = "Operations related to doctor blocked date management")
public class DoctorBlockedDateController {

    private final DoctorBlockedDateService doctorBlockedDateService;

    @Operation(summary = "Create blocked date for doctor")
    @ApiResponse(
            responseCode = "201",
            description = "The blocked date has been successfully created. The Location header contains the URI of the newly created resource.",
            content = @Content(schema = @Schema(implementation = DoctorBlockedDateResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "A blocked date already exists for the specified doctor on the given date. Duplicate blocked dates are not permitted.", content = @Content)
    @PreAuthorize("@doctorSecurity.isSelfOrAdmin(#doctorId)")
    @PostMapping
    public ResponseEntity<DoctorBlockedDateResponseDTO> createBlockedDate(
            @PathVariable UUID doctorId,
            @RequestBody @Valid CreateDoctorBlockedDateRequestDTO requestDTO
    ) {
        DoctorBlockedDateResponseDTO response =
                doctorBlockedDateService.createBlockedDate(doctorId, requestDTO);

        URI location = URI.create(
                "/api/v1/doctors/" + doctorId + "/blocked-dates/" + response.getId()
        );

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get blocked date by ID")
    @ApiResponse(
            responseCode = "200",
            description = "The blocked date record was retrieved successfully.",
            content = @Content(schema = @Schema(implementation = DoctorBlockedDateResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "No blocked date record was found for the provided identifier.", content = @Content)
    @GetMapping("/{blockedDateId}")
    public ResponseEntity<DoctorBlockedDateResponseDTO> getBlockedDateById(
            @PathVariable UUID blockedDateId
    ) {
        return ResponseEntity.ok(
                doctorBlockedDateService.getBlockedDateById(blockedDateId)
        );
    }

    @Operation(summary = "Get blocked dates for doctor")
    @ApiResponse(
            responseCode = "200",
            description = "A paginated list of blocked dates for the specified doctor was retrieved successfully.",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @GetMapping
    public ResponseEntity<PageResponse<DoctorBlockedDateResponseDTO>> getBlockedDatesByDoctor(
            @PathVariable UUID doctorId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorBlockedDateService.getBlockedDatesByDoctor(doctorId, pageable)
        );
    }

    @Operation(summary = "Get blocked dates by date range")
    @ApiResponse(
            responseCode = "200",
            description = "A paginated list of blocked dates within the specified date range was retrieved successfully.",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "The provided date range is invalid. Ensure both dates are in ISO format (yyyy-MM-dd).", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified doctor could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The start date must not be after the end date. Please provide a valid chronological date range.", content = @Content)
    @GetMapping("/range")
    public ResponseEntity<PageResponse<DoctorBlockedDateResponseDTO>> getBlockedDatesByDateRange(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorBlockedDateService.getBlockedDatesByDateRange(
                        doctorId,
                        startDate,
                        endDate,
                        pageable
                )
        );
    }

    @Operation(summary = "Delete blocked date")
    @ApiResponse(responseCode = "204", description = "The blocked date has been successfully deleted. No content is returned.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "No blocked date record was found for the provided identifier.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The blocked date cannot be deleted once the doctor's first time slot for that day has already started.", content = @Content)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @DeleteMapping("/{blockedDateId}")
    public ResponseEntity<Void> deleteBlockedDate(
            @PathVariable UUID blockedDateId
    ) {
        doctorBlockedDateService.deleteBlockedDate(blockedDateId);
        return ResponseEntity.noContent().build();
    }
}