package com.healthcare.hospitalmanagementapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Authentication response containing tokens")
public class AuthResponseDTO {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "Refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;
}