package com.healthcare.hospitalmanagementapi.doctor.dto.doctor;

import com.healthcare.hospitalmanagementapi.department.dto.DepartmentResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the doctor")
    private UUID id;

    @Schema(description = "Basic user information associated with this doctor")
    private UserSummaryDTO user;

    @Schema(description = "Department this doctor belongs to")
    private DepartmentResponseDTO department;

    @Schema(example = "MBBS, MD - General Medicine", description = "Qualifications of the doctor")
    private String qualification;

    @Schema(example = "Senior Consultant", description = "Designation of the doctor")
    private String designation;

    @Schema(example = "Cardiology", description = "Medical specialization of the doctor")
    private String specialization;

    @Schema(example = "A-204", description = "Consultation room number")
    private String roomNumber;

    @Schema(example = "30", description = "Number of days in advance appointments can be booked")
    private Integer advanceBookingDays;

    @Schema(example = "https://cdn.example.com/photos/doctor-uuid.jpg", description = "URL of the doctor's profile photo")
    private String photoUrl;

    @Schema(
            example = "true",
            description = "Indicates whether the doctor record has been soft deleted"
    )
    private boolean isDeleted;
}