package com.healthcare.hospitalmanagementapi.doctor.dto.doctor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {

    @Schema(example = "John", description = "First name of the user")
    private String firstName;

    @Schema(example = "Doe", description = "Last name of the user")
    private String lastName;

    @Schema(example = "john.doe@example.com", description = "Email address of the user")
    private String email;
}
