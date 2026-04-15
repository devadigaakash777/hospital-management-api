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
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
@Tag(name = "Doctor Blocked Date Management", description = "Operations related to doctor blocked date management")
public class DoctorBlockedDateController {

    private final DoctorBlockedDateService doctorBlockedDateService;

    @Operation(summary = "Create blocked date for doctor")
    @ApiResponse(
            responseCode = "201",
            description = "Blocked date created successfully",
            content = @Content(schema = @Schema(implementation = DoctorBlockedDateResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @ApiResponse(responseCode = "409", description = "Doctor already has a blocked date on the given date")
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
            description = "Blocked date fetched successfully",
            content = @Content(schema = @Schema(implementation = DoctorBlockedDateResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Blocked date not found", content = @Content)
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
            description = "Blocked dates fetched successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
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
            description = "Blocked dates by range fetched successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid date range", content = @Content)
    @ApiResponse(responseCode = "404", description = "Doctor not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @ApiResponse(responseCode = "409", description = "Start date cannot be after end date")
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
    @ApiResponse(responseCode = "204", description = "Blocked date deleted successfully")
    @ApiResponse(responseCode = "404", description = "Blocked date not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @ApiResponse(responseCode = "409", description = "Blocked date can only be deleted before the first doctor time slot starts")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @DeleteMapping("/{blockedDateId}")
    public ResponseEntity<Void> deleteBlockedDate(
            @PathVariable UUID blockedDateId
    ) {
        doctorBlockedDateService.deleteBlockedDate(blockedDateId);
        return ResponseEntity.noContent().build();
    }
}