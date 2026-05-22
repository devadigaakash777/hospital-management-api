package com.healthcare.hospitalmanagementapi.security.dto;

import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspiciousActivityLogResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the log entry")
    private UUID id;

    @Schema(example = "REVOKED_TOKEN_REUSE", description = "Category of the suspicious event")
    private SuspiciousEvent eventType;

    @Schema(example = "john.doe@example.com", description = "Email of the targeted account. Null if token was forged and not linked to any user")
    private String email;

    @Schema(example = "192.168.1.100", description = "Client IP address")
    private String ipAddress;

    @Schema(example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", description = "User-Agent header from the request")
    private String userAgent;

    @Schema(example = "a1b2c3d4", description = "First 8 characters of the refresh token used in the suspicious request")
    private String tokenHint;

    @Schema(example = "Revoked token reused — all active sessions wiped for user 3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Human-readable detail for investigators")
    private String detail;

    @Schema(example = "2026-05-21T10:55:00Z", description = "Timestamp when the event was recorded")
    private Instant createdAt;
}