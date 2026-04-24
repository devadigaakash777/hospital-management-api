package com.healthcare.hospitalmanagementapi.healthpackage.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageWeeklyScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HealthPackageAvailabilityValidator {

    private final HealthPackageWeeklyScheduleRepository healthPackageWeeklyScheduleRepository;

    public void validateAppointmentDateNotInPast(LocalDate appointmentDate) {
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new ConflictException("Appointment date cannot be in the past");
        }
    }

    public void validateAppointmentDateWithinAdvanceBookingLimit(
            HealthPackage healthPackage,
            LocalDate appointmentDate
    ) {
        LocalDate lastAllowedDate = LocalDate.now()
                .plusDays(healthPackage.getAdvanceBookingDays());

        if (appointmentDate.isAfter(lastAllowedDate)) {
            throw new ConflictException(
                    "Appointment date must be within "
                            + healthPackage.getAdvanceBookingDays()
                            + " days from today"
            );
        }
    }

    public void validateHealthPackageWeeklySchedule(
            HealthPackage healthPackage,
            LocalDate appointmentDate
    ) {
        int weekNumber = ((appointmentDate.getDayOfMonth() - 1) / 7) + 1;

        com.healthcare.hospitalmanagementapi.enums.DayOfWeek dayOfWeek =
                com.healthcare.hospitalmanagementapi.enums.DayOfWeek.valueOf(
                        appointmentDate.getDayOfWeek().name()
                );

        // First check the specific week number
        boolean available = healthPackageWeeklyScheduleRepository
                .existsByHealthPackageAndWeekNumberAndDayOfWeek(
                        healthPackage,
                        weekNumber,
                        dayOfWeek
                );

        // Fall back to week 0 (recurring every week on that day)
        if (!available) {
            available = healthPackageWeeklyScheduleRepository
                    .existsByHealthPackageAndWeekNumberAndDayOfWeek(
                            healthPackage,
                            0,
                            dayOfWeek
                    );
        }

        if (!available) {
            throw new ConflictException(
                    "Health package is not available on "
                            + dayOfWeek
                            + " of week "
                            + weekNumber
            );
        }
    }
}