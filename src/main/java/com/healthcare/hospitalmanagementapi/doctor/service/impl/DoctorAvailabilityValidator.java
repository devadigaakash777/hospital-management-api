package com.healthcare.hospitalmanagementapi.doctor.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorBlockedDateRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorBlockedTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorWeeklyScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityValidator {
    private final DoctorWeeklyScheduleRepository doctorWeeklyScheduleRepository;
    private final DoctorBlockedDateRepository doctorBlockedDateRepository;
    private final DoctorBlockedTimeSlotRepository doctorBlockedTimeSlotRepository;

    public void validateAppointmentDateNotInPast(LocalDate appointmentDate) {
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new ConflictException("Appointment date cannot be in the past");
        }
    }

    public void validateAppointmentDateWithinAdvanceBookingLimit(
            Doctor doctor,
            LocalDate appointmentDate
    ) {
        LocalDate lastAllowedDate = LocalDate.now()
                .plusDays(doctor.getAdvanceBookingDays());

        if (appointmentDate.isAfter(lastAllowedDate)) {
            throw new ConflictException(
                    "Appointment date must be within "
                            + doctor.getAdvanceBookingDays()
                            + " days from today"
            );
        }
    }

    public void validateDoctorWeeklySchedule(
            Doctor doctor,
            LocalDate appointmentDate
    ) {
        int weekNumber = ((appointmentDate.getDayOfMonth() - 1) / 7) + 1;

        com.healthcare.hospitalmanagementapi.enums.DayOfWeek dayOfWeek =
                com.healthcare.hospitalmanagementapi.enums.DayOfWeek.valueOf(
                        appointmentDate.getDayOfWeek().name()
                );

        boolean available = doctorWeeklyScheduleRepository
                .existsByDoctorAndWeekNumberAndDayOfWeek(
                        doctor,
                        weekNumber,
                        dayOfWeek
                );

        if (!available) {
            available = doctorWeeklyScheduleRepository
                    .existsByDoctorAndWeekNumberAndDayOfWeek(
                            doctor,
                            0,
                            dayOfWeek
                    );
        }

        if (!available) {
            throw new ConflictException(
                    "Doctor is not available on "
                            + dayOfWeek
                            + " of week "
                            + weekNumber
            );
        }
    }

    public void validateDoctorBlockedDate(
            Doctor doctor,
            LocalDate appointmentDate
    ) {
        boolean blocked = doctorBlockedDateRepository
                .existsByDoctor_IdAndBlockedDateAndIsDeletedFalse(
                        doctor.getId(),
                        appointmentDate
                );

        if (blocked) {
            throw new ConflictException(
                    "Doctor is not available on the selected date"
            );
        }
    }

    public void validateDoctorBlockedTimeSlot(
            Doctor doctor,
            DoctorTimeSlot doctorTimeSlot,
            LocalDate appointmentDate
    ) {
        boolean blocked = doctorBlockedTimeSlotRepository
                .existsByDoctor_IdAndBlockedDateAndStartTimeLessThanAndEndTimeGreaterThanAndIsDeletedFalse(
                        doctor.getId(),
                        appointmentDate,
                        doctorTimeSlot.getEndTime(),
                        doctorTimeSlot.getStartTime()
                );

        if (blocked) {
            throw new ConflictException(
                    "Selected doctor time slot is blocked for the appointment date"
            );
        }
    }
}
