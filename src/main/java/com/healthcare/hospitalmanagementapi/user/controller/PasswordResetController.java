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
    @ApiResponse(responseCode = "202", description = "The request has been accepted. If the provided email address is associated with an active account, a one-time password (OTP) will be dispatched to that address.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.")
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
    @ApiResponse(responseCode = "204", description = "The password has been successfully reset. The user may now authenticate using the new credentials. No content is returned.")
    @ApiResponse(responseCode = "400", description = "The request could not be processed. Possible causes include an invalid or expired OTP, or a new password that is identical to the current password.")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordRequestDTO request
    ) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}