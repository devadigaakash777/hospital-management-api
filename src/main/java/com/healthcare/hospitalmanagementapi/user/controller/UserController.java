package com.healthcare.hospitalmanagementapi.user.controller;

import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailChangeRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;
import com.healthcare.hospitalmanagementapi.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations related to user management")
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
public class UserController {

    private final UserService userService;

    @Operation(summary = "Initiate user creation — sends OTP to email")
    @ApiResponse(responseCode = "202", description = "The request has been accepted. A one-time password (OTP) has been dispatched to the provided email address to complete the registration process.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.")
    @ApiResponse(responseCode = "409", description = "The provided email address is already associated with an existing account.")
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping
    public ResponseEntity<Void> createUser(
            @RequestBody @Valid CreateUserRequestDTO request
    ) {
        userService.initiateUserCreation(request);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Verify email OTP — completes user creation")
    @ApiResponse(responseCode = "201", description = "The user account has been successfully created following OTP verification. The Location header contains the URI of the newly created resource.")
    @ApiResponse(responseCode = "400", description = "The provided OTP is invalid or has expired. Please request a new OTP to proceed.", content = @Content)
    @PostMapping("/verify-email")
    public ResponseEntity<UserResponseDTO> verifyEmail(
            @RequestBody @Valid VerifyEmailRequestDTO request
    ) {
        UserResponseDTO response = userService.verifyEmail(request);
        URI location = URI.create("/api/v1/users/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Resend OTP for a pending user")
    @ApiResponse(responseCode = "204", description = "A new OTP has been successfully dispatched to the email address associated with the pending registration. No content is returned.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.")
    @ApiResponse(responseCode = "404", description = "No pending registration was found for the provided email address.")
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resendOtp(
            @RequestParam String email
    ) {
        userService.resendOtp(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Initiate email change — sends OTP to new email address")
    @ApiResponse(responseCode = "202", description = "The request has been accepted. A one-time password (OTP) has been dispatched to the new email address to confirm the change.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed, or the provided email address is identical to the current one.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.")
    @ApiResponse(responseCode = "404", description = "The specified user could not be found or has been removed.")
    @ApiResponse(responseCode = "409", description = "The provided email address is already associated with an existing account.")
    @PreAuthorize("@userSecurity.isSelfOrAdmin(#userId) or hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping("/{userId}/change-email")
    public ResponseEntity<Void> initiateEmailChange(
            @PathVariable UUID userId,
            @RequestBody @Valid ChangeEmailRequestDTO request
    ) {
        userService.initiateEmailChange(userId, request);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Verify OTP — applies the email change")
    @ApiResponse(responseCode = "200", description = "The email address has been successfully updated following OTP verification. The updated user profile is returned in the response body.")
    @ApiResponse(responseCode = "400", description = "The provided OTP is invalid or has expired. Please request a new OTP to proceed.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified user could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The provided email address is already associated with an existing account.", content = @Content)
    @PostMapping("/verify-email-change")
    public ResponseEntity<UserResponseDTO> verifyEmailChange(
            @RequestBody @Valid VerifyEmailChangeRequestDTO request
    ) {
        return ResponseEntity.ok(userService.verifyEmailChange(request));
    }

    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "The user profile was retrieved successfully.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified user could not be found or has been removed.", content = @Content)
    @PreAuthorize("@userSecurity.isSelfOrAdmin(#userId) or hasAuthority('CAN_MANAGE_STAFF')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @Operation(summary = "Get current logged-in user")
    @ApiResponse(responseCode = "200", description = "The profile of the currently authenticated user was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The user account associated with the current session could not be found.", content = @Content)
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(userService.getUserById(userDetails.getUser().getId()));
    }

    @Operation(summary = "Get all users with pagination")
    @ApiResponse(responseCode = "200", description = "A paginated list of user profiles was retrieved successfully.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF') or hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @GetMapping
    public ResponseEntity<PageResponse<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @Operation(summary = "Update user (partial update supported)")
    @ApiResponse(responseCode = "200", description = "The user profile has been successfully updated. The updated resource is returned in the response body.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified user could not be found or has been removed.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF')")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid UpdateUserRequestDTO request
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @Operation(summary = "Delete user (soft delete)")
    @ApiResponse(responseCode = "204", description = "The user account has been successfully soft-deleted. No content is returned.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.")
    @ApiResponse(responseCode = "404", description = "The specified user could not be found or has been removed.")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_STAFF')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore user by email")
    @ApiResponse(responseCode = "200", description = "The user account has been successfully restored and is now active. The restored user profile is returned in the response body.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "No user account associated with the provided email address could be found.", content = @Content)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping("/restore")
    public ResponseEntity<UserResponseDTO> restoreUser(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(userService.restoreUserByEmail(email));
    }

    @Operation(summary = "Change user password")
    @ApiResponse(responseCode = "204", description = "The password has been successfully updated. The user may now authenticate using the new credentials. No content is returned.")
    @ApiResponse(responseCode = "400", description = "The current password is incorrect, the new password does not meet the required policy, or both passwords are identical.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.")
    @ApiResponse(responseCode = "404", description = "The specified user could not be found or has been removed.")
    @PreAuthorize("@userSecurity.isSelfOrAdmin(#userId) or hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping("/{userId}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID userId,
            @RequestBody @Valid ChangePasswordRequestDTO request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search users")
    @ApiResponse(responseCode = "200", description = "A paginated list of users matching the search criteria was retrieved successfully.")
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF') or hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<UserResponseDTO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.searchUsers(keyword, page, size));
    }
}