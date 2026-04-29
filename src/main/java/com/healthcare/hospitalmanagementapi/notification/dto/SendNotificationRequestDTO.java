package com.healthcare.hospitalmanagementapi.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SendNotificationRequestDTO {

    @NotEmpty
    private List<UUID> userIds;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private String data;
}