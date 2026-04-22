package com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDoctorBlockedTimeSlotRequestDTO {

    @Schema(example = "15:00:00", description = "Updated start time of the blocked period")
    private LocalTime startTime;

    @Schema(example = "17:00:00", description = "Updated end time of the blocked period")
    private LocalTime endTime;

    @Size(max = 1000, message = "Block reason must not exceed 1000 characters")
    @Schema(example = "Doctor delayed due to surgery", description = "Updated reason for blocking this time slot")
    private String blockReason;
}