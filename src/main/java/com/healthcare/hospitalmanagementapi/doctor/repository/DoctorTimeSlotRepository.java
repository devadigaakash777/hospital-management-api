package com.healthcare.hospitalmanagementapi.doctor.repository;

import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface DoctorTimeSlotRepository extends JpaRepository<DoctorTimeSlot, UUID> {

    List<DoctorTimeSlot> findAllByDoctorId(UUID doctorId);

    boolean existsByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID doctorId,
            LocalTime endTime,
            LocalTime startTime
    );

    boolean existsByDoctorIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID doctorId,
            UUID slotId,
            LocalTime endTime,
            LocalTime startTime
    );
}