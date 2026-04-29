package com.healthcare.hospitalmanagementapi.notification.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponseDTO {

    private UUID id;
    private String title;
    private String message;
    private String data;
    private Boolean isRead;
    private LocalDateTime createdAt;
}