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
public class ForgotPasswordRequestDTO {

    @NotBlank
    @Email
    @Size(max = 150)
    @Schema(example = "john.doe@example.com", description = "Registered email address")
    private String email;
}