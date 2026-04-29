package com.healthcare.hospitalmanagementapi.notification.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.notification.dto.*;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void registerDeviceToken(UUID userId, RegisterDeviceTokenRequestDTO dto);
    void removeDeviceToken(UUID userId, String fcmToken);

    void sendToUser(UUID userId, String title, String message, String data);
    void sendToUsers(List<UUID> userIds, String title, String message, String data);

    PageResponse<NotificationResponseDTO> getMyNotifications(UUID userId, int page, int size);
    long getUnreadCount(UUID userId);
    void markAllAsRead(UUID userId);
}