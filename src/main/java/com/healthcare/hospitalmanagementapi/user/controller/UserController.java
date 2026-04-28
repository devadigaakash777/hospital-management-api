package com.healthcare.hospitalmanagementapi.user.controller;

import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailChangeRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.email.VerifyEmailRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.*;
import com.healthcare.hospitalmanagementapi.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Initiate user creation — sends OTP to email")
    @ApiResponse(responseCode = "202", description = "OTP sent to email")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Email already exists")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping
    public ResponseEntity<Void> createUser(
            @RequestBody @Valid CreateUserRequestDTO request
    ) {
        userService.initiateUserCreation(request);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Verify email OTP — completes user creation")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @PostMapping("/verify-email")
    public ResponseEntity<UserResponseDTO> verifyEmail(
            @RequestBody @Valid VerifyEmailRequestDTO request
    ) {
        UserResponseDTO response = userService.verifyEmail(request);
        URI location = URI.create("/api/v1/users/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Resend OTP for a pending user")
    @ApiResponse(responseCode = "204", description = "OTP resent successfully")
    @ApiResponse(responseCode = "404", description = "No pending registration for this email")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resendOtp(
            @RequestParam String email
    ) {
        userService.resendOtp(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Initiate email change — sends OTP to new email address")
    @ApiResponse(responseCode = "202", description = "OTP sent to new email address")
    @ApiResponse(responseCode = "400", description = "New email is the same as current, or invalid input")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "New email already in use")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
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
    @ApiResponse(responseCode = "200", description = "Email updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "New email already in use")
    @PostMapping("/verify-email-change")
    public ResponseEntity<UserResponseDTO> verifyEmailChange(
            @RequestBody @Valid VerifyEmailChangeRequestDTO request
    ) {
        return ResponseEntity.ok(userService.verifyEmailChange(request));
    }

    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User fetched successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("@userSecurity.isSelfOrAdmin(#userId) or hasAuthority('CAN_MANAGE_STAFF')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @Operation(summary = "Get current logged-in user")
    @ApiResponse(responseCode = "200", description = "User fetched successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(userService.getUserById(userDetails.getUser().getId()));
    }

    @Operation(summary = "Get all users with pagination")
    @ApiResponse(responseCode = "200", description = "Users fetched successfully")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF') or hasAuthority('CAN_MANAGE_DOCTOR_SLOTS')")
    @GetMapping
    public ResponseEntity<PageResponse<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @Operation(summary = "Update user (partial update supported)")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF')")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid UpdateUserRequestDTO request
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @Operation(summary = "Delete user (soft delete)")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_STAFF')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore user by email")
    @ApiResponse(responseCode = "200", description = "User restored successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping("/restore")
    public ResponseEntity<UserResponseDTO> restoreUser(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(userService.restoreUserByEmail(email));
    }

    @Operation(summary = "Change user password")
    @ApiResponse(responseCode = "204", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid password")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
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
    @ApiResponse(responseCode = "200", description = "Users fetched successfully")
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