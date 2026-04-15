package com.healthcare.hospitalmanagementapi.doctor.dto.doctor;

import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.CreateDoctorWeeklyScheduleRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorRequestDTO {

    @NotNull(message = "User ID is required")
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the user account to associate with this doctor")
    private UUID userId;

    @NotNull(message = "Department ID is required")
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the department this doctor belongs to")
    private UUID departmentId;

    @NotBlank(message = "Qualification is required")
    @Size(max = 255, message = "Qualification must not exceed 255 characters")
    @Schema(example = "MBBS, MD - General Medicine", description = "Academic and professional qualifications of the doctor")
    private String qualification;

    @NotBlank(message = "Designation is required")
    @Size(max = 150, message = "Designation must not exceed 150 characters")
    @Schema(example = "Senior Consultant", description = "Job designation of the doctor")
    private String designation;

    @NotBlank(message = "Specialization is required")
    @Size(max = 150, message = "Specialization must not exceed 150 characters")
    @Schema(example = "Cardiology", description = "Medical specialization of the doctor")
    private String specialization;

    @Size(max = 20, message = "Room number must not exceed 20 characters")
    @Schema(example = "A-204", description = "Consultation room number assigned to the doctor")
    private String roomNumber;

    @NotNull(message = "Advance booking days is required")
    @Min(value = 1, message = "Advance booking days must be at least 1")
    @Max(value = 365, message = "Advance booking days must not exceed 365")
    @Schema(example = "30", description = "Number of days in advance that appointments can be booked")
    private Integer advanceBookingDays;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    @Schema(example = "https://cdn.example.com/photos/doctor-uuid.jpg", description = "URL of the doctor's profile photo")
    private String photoUrl;
}

