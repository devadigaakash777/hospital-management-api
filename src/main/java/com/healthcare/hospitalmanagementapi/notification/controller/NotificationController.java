package com.healthcare.hospitalmanagementapi.notification.controller;

import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.notification.dto.*;
import com.healthcare.hospitalmanagementapi.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Notifications", description = "Push notifications and in-app notification history")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Register FCM device token for the current user")
    @PostMapping("/device-token")
    public ResponseEntity<Void> registerDeviceToken(
            @RequestBody @Valid RegisterDeviceTokenRequestDTO request
    ) {
        notificationService.registerDeviceToken(getCurrentUserId(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove an FCM device token (on logout)")
    @DeleteMapping("/device-token")
    public ResponseEntity<Void> removeDeviceToken(@RequestParam String fcmToken) {
        notificationService.removeDeviceToken(getCurrentUserId(), fcmToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Send push notification to one or multiple users (admin only)")
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

    @Operation(summary = "Get my notifications (paginated)")
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponseDTO>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(getCurrentUserId(), page, size)
        );
    }

    @Operation(summary = "Get my unread notification count")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount(getCurrentUserId()));
    }

    @Operation(summary = "Mark all notifications as read")
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUser().getId();
    }
}