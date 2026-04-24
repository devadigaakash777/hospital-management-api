package com.healthcare.hospitalmanagementapi.healthpackage.repository;

import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageWeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HealthPackageWeeklyScheduleRepository extends JpaRepository<HealthPackageWeeklySchedule, UUID> {

    List<HealthPackageWeeklySchedule> findAllByHealthPackageId(UUID healthPackageId);

    Optional<HealthPackageWeeklySchedule> findByIdAndHealthPackageId(UUID id, UUID healthPackageId);

    boolean existsByHealthPackageAndWeekNumberAndDayOfWeek(
            HealthPackage healthPackage,
            Integer weekNumber,
            DayOfWeek dayOfWeek
    );

    void deleteAllByHealthPackageId(UUID healthPackageId);
}