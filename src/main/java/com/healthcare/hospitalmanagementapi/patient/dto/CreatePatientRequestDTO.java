package com.healthcare.hospitalmanagementapi.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class CreatePatientRequestDTO {

    @NotBlank
    @Size(max = 150)
    @Schema(
            description = "Patient first name",
            example = "James"
    )
    private String firstName;

    @Size(max = 150)
    @Schema(
            description = "Patient last name",
            example = "Doe"
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
            example = "james@example.com"
    )
    private String email;
}