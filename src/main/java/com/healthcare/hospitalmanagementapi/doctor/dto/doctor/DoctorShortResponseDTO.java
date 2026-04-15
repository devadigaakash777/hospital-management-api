package com.healthcare.hospitalmanagementapi.doctor.dto.doctor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Basic doctor information used for search suggestions")
public class DoctorShortResponseDTO {

    @Schema(
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            description = "Unique identifier of the doctor"
    )
    private UUID id;

    @Schema(
            example = "Dr. John Doe",
            description = "Full name of the doctor"
    )
    private String fullName;
}
