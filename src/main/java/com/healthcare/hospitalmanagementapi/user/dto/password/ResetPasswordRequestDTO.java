package com.healthcare.hospitalmanagementapi.user.dto.password;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequestDTO {

    @NotBlank
    @Email
    @Size(max = 150)
    @Schema(example = "john.doe@example.com", description = "Registered email address")
    private String email;

    @NotBlank
    @Size(min = 6, max = 6)
    @Schema(example = "483920", description = "OTP sent to the email")
    private String otp;

    @NotBlank
    @Size(min = 8, max = 255)
    @Schema(example = "NewSecurePassword456", description = "New password to set")
    private String newPassword;
}