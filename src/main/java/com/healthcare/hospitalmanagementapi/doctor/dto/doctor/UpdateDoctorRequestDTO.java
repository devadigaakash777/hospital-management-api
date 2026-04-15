package com.healthcare.hospitalmanagementapi.doctor.dto.doctor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDoctorRequestDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the department to reassign this doctor to")
    private UUID departmentId;

    @Size(max = 255, message = "Qualification must not exceed 255 characters")
    @Schema(example = "MBBS, MD - Cardiology", description = "Updated qualifications of the doctor")
    private String qualification;

    @Size(max = 150, message = "Designation must not exceed 150 characters")
    @Schema(example = "Chief Consultant", description = "Updated designation of the doctor")
    private String designation;

    @Size(max = 150, message = "Specialization must not exceed 150 characters")
    @Schema(example = "Interventional Cardiology", description = "Updated medical specialization")
    private String specialization;

    @Size(max = 20, message = "Room number must not exceed 20 characters")
    @Schema(example = "B-301", description = "Updated consultation room number")
    private String roomNumber;

    @Min(value = 1, message = "Advance booking days must be at least 1")
    @Max(value = 365, message = "Advance booking days must not exceed 365")
    @Schema(example = "14", description = "Updated number of days in advance that appointments can be booked")
    private Integer advanceBookingDays;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    @Schema(example = "https://cdn.example.com/photos/doctor-uuid-v2.jpg", description = "Updated URL of the doctor's profile photo")
    private String photoUrl;
}