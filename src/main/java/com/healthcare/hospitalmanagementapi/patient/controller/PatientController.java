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
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content)
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
            description = "Patient registered successfully. A unique UH ID has been assigned to the patient and, if an email address was provided, a welcome notification has been dispatched. " +
                    "The Location header contains the URI of the newly created resource.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PatientResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body is invalid or one or more required fields failed validation.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "A patient record with conflicting identifying information already exists in the system.",
            content = @Content
    )
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
            description = "Patient record retrieved successfully.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PatientResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active patient was found with the provided identifier. The patient may have been deleted or may never have existed.",
            content = @Content
    )
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
            description = "Paginated list of patients matching the search term retrieved successfully. Returns an empty page if no records match.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The pagination parameters are invalid.",
            content = @Content
    )
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
            description = "Paginated list of all active patient records retrieved successfully.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
            )
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
            description = "Patient record updated successfully. Returns the full updated patient record.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PatientResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body is invalid or one or more fields failed validation.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active patient was found with the provided identifier.",
            content = @Content
    )
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
    @ApiResponse(
            responseCode = "204",
            description = "Patient soft-deleted successfully. The record is retained in the system and can be restored if required."
    )
    @ApiResponse(
            responseCode = "404",
            description = "No active patient was found with the provided identifier.",
            content = @Content
    )
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
            description = "Patient record restored successfully. Returns the full patient record in its reinstated active state.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PatientResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No patient was found with the provided identifier.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "The patient record is already in an active state and cannot be restored again.",
            content = @Content
    )
    @PostMapping("/{patientId}/restore")
    public ResponseEntity<PatientResponseDTO> restorePatient(
            @Parameter(description = "Patient ID")
            @PathVariable UUID patientId
    ) {
        PatientResponseDTO response = patientService.restorePatient(patientId);

        return ResponseEntity.ok(response);
    }
}