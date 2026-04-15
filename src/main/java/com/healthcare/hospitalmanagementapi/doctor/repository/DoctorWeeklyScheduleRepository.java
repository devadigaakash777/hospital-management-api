package com.healthcare.hospitalmanagementapi.doctor.repository;

import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorWeeklySchedule;
import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorWeeklyScheduleRepository extends JpaRepository<DoctorWeeklySchedule, UUID> {

    List<DoctorWeeklySchedule> findAllByDoctorId(UUID doctorId);

    Optional<DoctorWeeklySchedule> findByIdAndDoctorId(UUID id, UUID doctorId);

    boolean existsByDoctorAndWeekNumberAndDayOfWeek(
            Doctor doctor,
            Integer weekNumber,
            DayOfWeek dayOfWeek
    );

    void deleteAllByDoctorId(UUID doctorId);
}