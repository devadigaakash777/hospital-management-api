package com.healthcare.hospitalmanagementapi.notification.controller;

import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.notification.dto.*;
import com.healthcare.hospitalmanagementapi.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in", content = @Content)
@Tag(name = "Notifications", description = "Push notifications and in-app notification history")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Register FCM device token",
            description = "Registers a device token for push notifications for the current user"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Device token registered successfully. Push notifications will be delivered to this device going forward."
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body is invalid or the FCM token field is missing or malformed.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "The authenticated user could not be found in the system.",
            content = @Content
    )
    @PostMapping("/device-token")
    public ResponseEntity<Void> registerDeviceToken(
            @RequestBody @Valid RegisterDeviceTokenRequestDTO request
    ) {
        notificationService.registerDeviceToken(getCurrentUserId(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remove FCM device token",
            description = "Removes a device token for the current user during logout"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Device token removed successfully. Push notifications will no longer be delivered to this device."
    )
    @ApiResponse(
            responseCode = "400",
            description = "The fcmToken query parameter is missing or invalid.",
            content = @Content
    )
    @DeleteMapping("/device-token")
    public ResponseEntity<Void> removeDeviceToken(@RequestParam String fcmToken) {
        notificationService.removeDeviceToken(getCurrentUserId(), fcmToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Send notification",
            description = "Send push notification to one or multiple users (Admin only)"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Notification dispatched successfully. The message has been persisted and a push notification has been sent to all registered devices of the target users."
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body is invalid — one or more required fields are missing or malformed.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied — only users with the ADMIN role are permitted to send notifications.",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "One or more of the specified target users could not be found.",
            content = @Content
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(
            @RequestBody @Valid SendNotificationRequestDTO request
    ) {
        notificationService.sendToUsers(
                request.getUserIds(),
                request.getTitle(),
                request.getMessage(),
                request.getData()
        );
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get my notifications",
            description = "Fetch paginated list of notifications for the current user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of notifications for the authenticated user retrieved successfully. Results are ordered by creation date descending (most recent first).",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The pagination parameters (page or size) are invalid.",
            content = @Content
    )
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponseDTO>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(getCurrentUserId(), page, size)
        );
    }

    @Operation(
            summary = "Get unread notification count",
            description = "Fetch total unread notifications count for the current user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Unread notification count retrieved successfully. Returns the total number of notifications not yet marked as read by the authenticated user.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Long.class)
            )
    )
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount(getCurrentUserId()));
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Mark a specific notification as read"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Notification marked as read successfully. Subsequent calls for the same notification ID are idempotent."
    )
    @ApiResponse(
            responseCode = "404",
            description = "No notification was found with the provided identifier, or it does not belong to the authenticated user.",
            content = @Content
    )
    @PostMapping("/{notificationId}/mark-read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(getCurrentUserId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all notifications as read for the current user"
    )
    @ApiResponse(
            responseCode = "204",
            description = "All notifications for the authenticated user have been marked as read successfully."
    )
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUser().getId();
    }
}