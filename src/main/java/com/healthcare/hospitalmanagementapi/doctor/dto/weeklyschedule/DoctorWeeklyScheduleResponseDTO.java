package com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule;

import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorWeeklyScheduleResponseDTO {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "Unique identifier of the weekly schedule entry")
    private UUID id;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", description = "ID of the doctor this schedule entry belongs to")
    private UUID doctorId;

    @Schema(example = "1", description = "Week number in the schedule cycle")
    private Integer weekNumber;

    @Schema(example = "MONDAY", description = "Day of the week the doctor is available")
    private DayOfWeek dayOfWeek;
}