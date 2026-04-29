package com.healthcare.hospitalmanagementapi.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponseDTO {

    @Schema(
            example = "550e8400-e29b-41d4-a716-446655440000",
            description = "Unique identifier of the notification"
    )
    private UUID id;

    @Schema(
            example = "Appointment Reminder",
            description = "Title of the notification"
    )
    private String title;

    @Schema(
            example = "Your appointment is scheduled for tomorrow at 10 AM",
            description = "Notification message content"
    )
    private String message;

    @Schema(
            example = "{\"appointmentId\":\"123\"}",
            description = "Additional data payload in JSON format"
    )
    private String data;

    @Schema(
            example = "false",
            description = "Indicates whether the notification has been read"
    )
    private Boolean isRead;

    @Schema(
            example = "2026-04-29T10:15:30",
            description = "Timestamp when the notification was created"
    )
    private LocalDateTime createdAt;
}