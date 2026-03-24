package com.healthcare.hospitalmanagementapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Login request payload")
public class LoginRequestDTO {

    @Schema(description = "User email", example = "user@example.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "User password", example = "password123")
    @NotBlank
    private String password;
}