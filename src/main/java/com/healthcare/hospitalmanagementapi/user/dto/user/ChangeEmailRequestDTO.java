package com.healthcare.hospitalmanagementapi.user.dto.user;

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
public class ChangeEmailRequestDTO {

    @Size(max = 150)
    @NotBlank
    @Email
    @Schema(example = "john.new@example.com", description = "New email address to set")
    private String newEmail;
}