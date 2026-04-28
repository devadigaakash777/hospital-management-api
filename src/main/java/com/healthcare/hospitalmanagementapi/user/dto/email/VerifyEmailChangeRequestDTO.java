package com.healthcare.hospitalmanagementapi.user.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyEmailChangeRequestDTO {

    @NotNull
    private UUID userId;

    @NotBlank
    @Email
    private String newEmail;

    @NotBlank
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String otp;
}