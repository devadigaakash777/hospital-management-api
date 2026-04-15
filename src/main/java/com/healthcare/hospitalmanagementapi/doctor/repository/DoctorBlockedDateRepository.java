package com.healthcare.hospitalmanagementapi.doctor.repository;

import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorBlockedDate;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DoctorBlockedDateRepository extends JpaRepository<DoctorBlockedDate, UUID> {

    Optional<DoctorBlockedDate> findByIdAndIsDeletedFalse(UUID id);

    @NotNull Optional<DoctorBlockedDate> findById(@NotNull UUID id);

    boolean existsByDoctor_IdAndBlockedDateAndIsDeletedFalse(UUID doctorId, LocalDate blockedDate);

    Page<DoctorBlockedDate> findAllByDoctor_IdAndIsDeletedFalse(UUID doctorId, Pageable pageable);

    Page<DoctorBlockedDate> findAllByDoctor_IdAndBlockedDateBetweenAndIsDeletedFalse(
            UUID doctorId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
}
