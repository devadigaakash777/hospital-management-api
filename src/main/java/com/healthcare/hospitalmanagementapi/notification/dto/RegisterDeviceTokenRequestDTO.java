package com.healthcare.hospitalmanagementapi.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterDeviceTokenRequestDTO {

    @NotBlank
    @Schema(example = "fcm_token_xyz123", description = "FCM device token from React Native")
    private String fcmToken;

    @Schema(example = "ANDROID", description = "Device type: ANDROID or IOS")
    private String deviceType;
}