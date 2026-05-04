package com.healthcare.hospitalmanagementapi.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SendNotificationRequestDTO {

    @NotEmpty
    @Schema(
            example = "[\"550e8400-e29b-41d4-a716-446655440000\"]",
            description = "List of user IDs to whom the notification will be sent"
    )
    private List<UUID> userIds;

    @NotBlank
    @Schema(
            example = "Appointment Reminder",
            description = "Title of the notification"
    )
    private String title;

    @NotBlank
    @Schema(
            example = "Your appointment is scheduled for tomorrow at 10 AM",
            description = "Notification message content"
    )
    private String message;

    @Schema(
            example = "{\"appointmentId\":\"123\"}",
            description = "Optional additional data payload as key-value pairs"
    )
    private Map<String, String> data;
}