package com.healthcare.hospitalmanagementapi.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePatientRequestDTO {

    @Size(max = 150)
    @Schema(
            description = "Patient first name",
            example = "Akash"
    )
    private String firstName;

    @Size(max = 150)
    @Schema(
            description = "Patient last name",
            example = "Devadiga"
    )
    private String lastName;

    @Size(max = 20)
    @Schema(
            description = "Patient phone number",
            example = "+919876543210"
    )
    private String phoneNumber;

    @Email
    @Size(max = 150)
    @Schema(
            description = "Patient email address",
            example = "akash@example.com"
    )
    private String email;
}