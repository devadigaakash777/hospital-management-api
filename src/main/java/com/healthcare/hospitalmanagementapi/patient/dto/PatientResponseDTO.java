package com.healthcare.hospitalmanagementapi.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponseDTO {

    @Schema(
            description = "Unique patient identifier"
    )
    private UUID id;

    @Schema(
            description = "Patient first name",
            example = "Akash"
    )
    private String firstName;

    @Schema(
            description = "Patient last name",
            example = "Devadiga"
    )
    private String lastName;

    @Schema(
            description = "Unique hospital UH ID",
            example = "UH-2026-0001"
    )
    private String uhId;

    @Schema(
            description = "Patient phone number",
            example = "+919876543210"
    )
    private String phoneNumber;

    @Schema(
            description = "Patient email address",
            example = "akash@example.com"
    )
    private String email;
}