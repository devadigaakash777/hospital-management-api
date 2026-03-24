package com.healthcare.hospitalmanagementapi.user.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDTO {

    @NotBlank
    @Size(min = 8, max = 255)
    @Schema(example = "OldPassword123", description = "Current password of the user")
    private String oldPassword;

    @NotBlank
    @Size(min = 8, max = 255)
    @Schema(example = "NewSecurePassword456", description = "New password to set")
    private String newPassword;
}