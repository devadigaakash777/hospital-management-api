package com.healthcare.hospitalmanagementapi.healthpackage.repository;

import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface HealthPackageTimeSlotRepository extends JpaRepository<HealthPackageTimeSlot, UUID> {

    List<HealthPackageTimeSlot> findAllByHealthPackageId(UUID healthPackageId);

    boolean existsByHealthPackageIdAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID healthPackageId,
            LocalTime endTime,
            LocalTime startTime
    );

    boolean existsByHealthPackageIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID healthPackageId,
            UUID slotId,
            LocalTime endTime,
            LocalTime startTime
    );
}