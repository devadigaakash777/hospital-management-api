package com.healthcare.hospitalmanagementapi.user.controller;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.user.dto.group.*;
import com.healthcare.hospitalmanagementapi.user.service.UserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-groups")
@RequiredArgsConstructor
@Tag(name = "User Group Management", description = "Operations related to user group management")
@ApiResponse(responseCode = "401", description = "Authentication required. The request lacks valid credentials or the session has expired.", content = @Content)
public class UserGroupController {

    private final UserGroupService userGroupService;

    @Operation(summary = "Create a new user group")
    @ApiResponse(responseCode = "201", description = "The user group has been successfully created. The Location header contains the URI of the newly created resource.")
    @ApiResponse(responseCode = "400", description = "The request payload is malformed or contains invalid field values. Refer to the error details for correction.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The request contains duplicate department identifiers. Each department may only be associated with a group once.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_GROUPS')")
    @PostMapping
    public ResponseEntity<UserGroupResponseDTO> createUserGroup(
            @RequestBody @Valid CreateUserGroupRequestDTO request
    ) {
        UserGroupResponseDTO response = userGroupService.createUserGroup(request);
        URI location = URI.create("/api/v1/user-groups/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get user group by ID")
    @ApiResponse(responseCode = "200", description = "The user group was retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "The specified user group could not be found or has been removed.", content = @Content)
    @GetMapping("/{groupId}")
    public ResponseEntity<UserGroupResponseDTO> getUserGroupById(
            @PathVariable UUID groupId
    ) {
        return ResponseEntity.ok(userGroupService.getUserGroupById(groupId));
    }

    @Operation(summary = "Get all user groups with pagination")
    @ApiResponse(responseCode = "200", description = "A paginated list of active user groups was retrieved successfully.")
    @GetMapping
    public ResponseEntity<PageResponse<UserGroupResponseDTO>> getAllUserGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userGroupService.getAllUserGroups(page, size));
    }

    @Operation(summary = "Update user group (partial update)")
    @ApiResponse(responseCode = "200", description = "The user group has been successfully updated. The updated resource is returned in the response body.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified user group could not be found or has been removed.", content = @Content)
    @ApiResponse(responseCode = "409", description = "The request contains duplicate department identifiers. Each department may only be associated with a group once.", content = @Content)
    @PreAuthorize("hasAuthority('CAN_MANAGE_GROUPS')")
    @PatchMapping("/{groupId}")
    public ResponseEntity<UserGroupResponseDTO> updateUserGroup(
            @PathVariable UUID groupId,
            @RequestBody @Valid UpdateUserGroupRequestDTO request
    ) {
        return ResponseEntity.ok(userGroupService.updateUserGroup(groupId, request));
    }

    @Operation(summary = "Delete user group (soft delete)")
    @ApiResponse(responseCode = "204", description = "The user group has been successfully soft-deleted. No content is returned.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified user group could not be found or has been removed.", content = @Content)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_GROUPS')")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteUserGroup(
            @PathVariable UUID groupId
    ) {
        userGroupService.deleteUserGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore user group by ID")
    @ApiResponse(responseCode = "200", description = "The user group has been successfully restored and is now active. The restored resource is returned in the response body.")
    @ApiResponse(responseCode = "403", description = "Access denied. The authenticated user does not have sufficient privileges to perform this operation.", content = @Content)
    @ApiResponse(responseCode = "404", description = "The specified user group could not be found.", content = @Content)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CAN_MANAGE_GROUPS')")
    @PostMapping("/{groupId}/restore")
    public ResponseEntity<UserGroupResponseDTO> restoreUserGroup(
            @PathVariable UUID groupId
    ) {
        return ResponseEntity.ok(userGroupService.restoreUserGroup(groupId));
    }
}