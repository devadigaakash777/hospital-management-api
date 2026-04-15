package com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule;

import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorWeeklyScheduleRequestDTO {

    @NotNull(message = "Week number is required")
    @Min(value = 0, message = "Week number must be at least 0")
    @Max(value = 5, message = "Week number must not exceed 5")
    @Schema(example = "1", description = "Week number within the schedule cycle (e.g. 1 for first week, 2 for second week)")
    private Integer weekNumber;

    @NotNull(message = "Day of week is required")
    @Schema(example = "MONDAY", description = "Day of the week the doctor is available")
    private DayOfWeek dayOfWeek;
}
