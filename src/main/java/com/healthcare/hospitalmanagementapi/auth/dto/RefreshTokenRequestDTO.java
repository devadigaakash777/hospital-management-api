package com.healthcare.hospitalmanagementapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Refresh token request payload")
public class RefreshTokenRequestDTO {

    @Schema(description = "Refresh token", example = "uuid-refresh-token")
    @NotBlank
    private String refreshToken;
}