package com.healthcare.hospitalmanagementapi.doctor.repository;

import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.DoctorBlockedTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.UpdateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorBlockedTimeSlot;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface DoctorBlockedTimeSlotRepository extends JpaRepository<DoctorBlockedTimeSlot, UUID> {

    Optional<DoctorBlockedTimeSlot> findByIdAndIsDeletedFalse(UUID id);

    @NotNull Optional<DoctorBlockedTimeSlot> findById(@NotNull UUID id);

    boolean existsByDoctor_IdAndBlockedDateAndStartTimeLessThanAndEndTimeGreaterThanAndIsDeletedFalse(
            UUID doctorId,
            LocalDate blockedDate,
            LocalTime endTime,
            LocalTime startTime
    );

    Page<DoctorBlockedTimeSlot> findAllByDoctor_IdAndIsDeletedFalse(UUID doctorId, Pageable pageable);

    Page<DoctorBlockedTimeSlot> findAllByDoctor_IdAndBlockedDateAndIsDeletedFalse(
            UUID doctorId,
            LocalDate blockedDate,
            Pageable pageable
    );
}
