package com.healthcare.hospitalmanagementapi.user.controller;

import com.healthcare.hospitalmanagementapi.user.dto.password.ForgotPasswordRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.password.ResetPasswordRequestDTO;
import com.healthcare.hospitalmanagementapi.user.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "Forgot-password and OTP-based password reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "Initiate forgot-password — sends OTP to registered email",
            description = "Always returns 202 to prevent email enumeration. " +
                    "An OTP is sent only if the email belongs to an active account."
    )
    @ApiResponse(responseCode = "202", description = "If the email is registered, an OTP has been sent")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequestDTO request
    ) {
        passwordResetService.initiateForgotPassword(request);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Reset password — verifies OTP and sets new password"
    )
    @ApiResponse(responseCode = "204", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP, or new password same as old")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordRequestDTO request
    ) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}