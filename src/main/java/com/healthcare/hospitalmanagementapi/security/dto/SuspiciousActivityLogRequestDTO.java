package com.healthcare.hospitalmanagementapi.security.dto;

import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Payload used internally to record a suspicious authentication event")
public class SuspiciousActivityLogRequestDTO {

    @Schema(example = "BRUTE_FORCE_DETECTED", description = "Category of the suspicious event")
    private final SuspiciousEvent eventType;

    @Schema(example = "john.doe@example.com", description = "Email of the targeted account. Null if the token was not linked to any user")
    private final String email;

    @Schema(example = "192.168.1.100", description = "Client IP address extracted from the request")
    private final String ipAddress;

    @Schema(example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", description = "User-Agent header from the request")
    private final String userAgent;

    @Schema(example = "a1b2c3d4", description = "First 8 characters of the refresh token — never the full value")
    private final String tokenHint;

    @Schema(example = "Revoked token reused — all active sessions wiped for user 3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Human-readable detail for investigators")
    private final String detail;
}