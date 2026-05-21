package com.healthcare.hospitalmanagementapi.auth.controller;

import com.healthcare.hospitalmanagementapi.auth.dto.*;
import com.healthcare.hospitalmanagementapi.auth.service.AuthService;
import com.healthcare.hospitalmanagementapi.common.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@ApiResponse(
        responseCode = "429",
        description = "Too many requests. Auth endpoints are limited to 5 requests per minute per IP.",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
        )
)
@Tag(name = "Authentication", description = "Authentication APIs for login, refresh and logout")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login user", description = "Authenticate user and return access & refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful. Returns a Bearer access token and a refresh token. " +
                            "The access token should be included in the Authorization header of subsequent requests.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request body is invalid or missing required fields (e.g. email or password).",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed — the provided email or password is incorrect.",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Refresh token", description = "Generate new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token rotation successful. Returns a new Bearer access token along with a new refresh token. " +
                            "The previous refresh token is immediately invalidated upon successful rotation.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request body is invalid or the refresh token field is missing.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token refresh failed — the provided refresh token is invalid, expired, or has already been revoked.",
                    content = @Content
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "Logout user", description = "Invalidate refresh token and logout user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful. The provided refresh token has been permanently revoked. " +
                            "Any access tokens previously issued remain valid until their natural expiry.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Logout failed — the provided refresh token is invalid or has already been revoked.",
                    content = @Content
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequestDTO request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}