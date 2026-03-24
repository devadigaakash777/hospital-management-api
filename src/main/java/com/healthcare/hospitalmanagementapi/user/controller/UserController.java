package com.healthcare.hospitalmanagementapi.user.controller;

import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
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

    @Operation(summary = "Create a new user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Only ADMIN can create another ADMIN user")
    @ApiResponse(responseCode = "403", description = "You do not have permission")
    @PreAuthorize("hasAuthority('CAN_MANAGE_STAFF')")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @RequestBody @Valid CreateUserRequestDTO request
    ) {
        UserResponseDTO response = userService.createUser(request);
        URI location = URI.create("/api/v1/users/" + response.getId());
        return ResponseEntity.created(location).body(response);
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

        return ResponseEntity.ok(
                userService.getUserById(userDetails.getUser().getId())
        );
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
}