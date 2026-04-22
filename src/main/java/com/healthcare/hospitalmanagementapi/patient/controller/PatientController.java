package com.healthcare.hospitalmanagementapi.patient.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.patient.dto.CreatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.PatientResponseDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.UpdatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(
        name = "Patient Management",
        description = "Operations related to patient management"
)
public class PatientController {

    private final PatientService patientService;

    @Operation(
            summary = "Create a new patient",
            description = "Creates a new patient and returns the created patient details."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Patient created successfully",
            content = @Content(schema = @Schema(implementation = PatientResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Patient already exists")
    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(
            @RequestBody @Valid CreatePatientRequestDTO requestDTO
    ) {
        PatientResponseDTO response = patientService.createPatient(requestDTO);

        URI location = URI.create("/api/v1/patients/" + response.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @Operation(
            summary = "Get patient by ID",
            description = "Fetches a patient by their unique identifier."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Patient retrieved successfully",
            content = @Content(schema = @Schema(implementation = PatientResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Patient not found")
    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponseDTO> getPatientById(
            @Parameter(description = "Patient ID")
            @PathVariable UUID patientId
    ) {
        PatientResponseDTO response = patientService.getPatientById(patientId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Search patients",
            description = "Search patients by UH ID, first name, or last name with pagination."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Patients retrieved successfully"
    )
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<PatientResponseDTO>> searchPatients(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable
    ) {
        PageResponse<PatientResponseDTO> response = patientService.searchPatients(search, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all patients",
            description = "Fetches all active patients with pagination."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Patients retrieved successfully"
    )
    @GetMapping
    public ResponseEntity<PageResponse<PatientResponseDTO>> getAllPatients(
            @ParameterObject Pageable pageable
    ) {
        PageResponse<PatientResponseDTO> response = patientService.getAllPatients(pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update patient",
            description = "Updates an existing patient."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Patient updated successfully",
            content = @Content(schema = @Schema(implementation = PatientResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Patient not found")
    @PatchMapping("/{patientId}")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @Parameter(description = "Patient ID")
            @PathVariable UUID patientId,
            @RequestBody @Valid UpdatePatientRequestDTO requestDTO
    ) {
        PatientResponseDTO response = patientService.updatePatient(patientId, requestDTO);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete patient",
            description = "Soft deletes a patient."
    )
    @ApiResponse(responseCode = "204", description = "Patient deleted successfully")
    @ApiResponse(responseCode = "404", description = "Patient not found")
    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Patient ID")
            @PathVariable UUID patientId
    ) {
        patientService.deletePatient(patientId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Restore patient",
            description = "Restores a previously soft deleted patient."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Patient restored successfully",
            content = @Content(schema = @Schema(implementation = PatientResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Patient not found")
    @ApiResponse(responseCode = "409", description = "Patient is already active")
    @PostMapping("/{patientId}/restore")
    public ResponseEntity<PatientResponseDTO> restorePatient(
            @Parameter(description = "Patient ID")
            @PathVariable UUID patientId
    ) {
        PatientResponseDTO response = patientService.restorePatient(patientId);

        return ResponseEntity.ok(response);
    }
}