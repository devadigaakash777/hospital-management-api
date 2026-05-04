package com.healthcare.hospitalmanagementapi.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.notification.dto.*;
import com.healthcare.hospitalmanagementapi.notification.entity.Notification;
import com.healthcare.hospitalmanagementapi.notification.entity.UserDeviceToken;
import com.healthcare.hospitalmanagementapi.notification.repository.NotificationRepository;
import com.healthcare.hospitalmanagementapi.notification.repository.UserDeviceTokenRepository;
import com.healthcare.hospitalmanagementapi.notification.service.FcmPushService;
import com.healthcare.hospitalmanagementapi.notification.service.NotificationService;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final UserDeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final FcmPushService fcmPushService;
    private final ObjectMapper objectMapper;


    @Override
    public void registerDeviceToken(UUID userId, RegisterDeviceTokenRequestDTO dto) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserDeviceToken token = deviceTokenRepository
                .findByFcmToken(dto.getFcmToken())
                .orElse(UserDeviceToken.builder().fcmToken(dto.getFcmToken()).build());

        token.setUser(user);
        token.setDeviceType(dto.getDeviceType());
        deviceTokenRepository.save(token);

        log.info("Device token registered for user {}", userId);
    }

    @Override
    public void removeDeviceToken(UUID userId, String fcmToken) {
        deviceTokenRepository.findByFcmToken(fcmToken)
                .filter(t -> t.getUser().getId().equals(userId))
                .ifPresent(deviceTokenRepository::delete);
        log.info("Device token removed for user {}", userId);
    }


    @Override
    public void sendToUser(UUID userId, String title, String message, Map<String, String> data) {
        sendToUsers(List.of(userId), title, message, data);
    }

    @Override
    public void sendToUsers(List<UUID> userIds, String title, String message, Map<String, String> data) {
        List<User> users = userRepository.findAllById(userIds);

        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .data(data)
                        .isRead(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);

        List<String> tokens = deviceTokenRepository
                .findAllByUserIdIn(userIds)
                .stream()
                .map(UserDeviceToken::getFcmToken)
                .toList();

        fcmPushService.sendToTokens(tokens, title, message, data);
    }

    @Override
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            log.debug("Notification {} already read — no-op", notificationId);
            return;
        }

        notificationRepository.markAsReadByIdAndUserId(notificationId, userId);
        log.info("Marked notification {} as read for user {}", notificationId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDTO> getMyNotifications(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> result = notificationRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDTO);
        return new PageResponse<>(result);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("Marked all notifications as read for user {}", userId);
    }

    private NotificationResponseDTO toDTO(Notification n) {
        String dataJson = null;
        if (n.getData() != null) {
            try {
                dataJson = objectMapper.writeValueAsString(n.getData());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize notification data for id {}", n.getId());
            }
        }

        return NotificationResponseDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .data(dataJson)
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}