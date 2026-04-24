package com.healthcare.hospitalmanagementapi.healthpackage.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.BadRequestException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.CreateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.HealthPackageTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.UpdateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageTimeSlot;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageTimeSlotMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageAppointmentRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageTimeSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "healthPackageTimeSlots")
public class HealthPackageTimeSlotServiceImpl implements HealthPackageTimeSlotService {

    private final HealthPackageAvailabilityValidator availabilityValidator;
    private final HealthPackageRepository healthPackageRepository;
    private final HealthPackageTimeSlotRepository timeSlotRepository;
    private final HealthPackageTimeSlotMapper timeSlotMapper;
    private final HealthPackageAppointmentRepository appointmentRepository;

    @Override
    @CacheEvict(key = "#healthPackageId")
    public HealthPackageTimeSlotResponseDTO create(
            UUID healthPackageId,
            CreateHealthPackageTimeSlotRequestDTO requestDTO
    ) {
        HealthPackage healthPackage = healthPackageRepository.findById(healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health package not found with id: " + healthPackageId
                ));

        HealthPackageTimeSlot slot = timeSlotMapper.toEntity(requestDTO);
        validateTimeSlot(healthPackageId, null, slot);
        slot.setHealthPackage(healthPackage);

        HealthPackageTimeSlot savedSlot = timeSlotRepository.save(slot);

        log.info("Created time slot {} for health package {}", savedSlot.getId(), healthPackageId);

        return timeSlotMapper.toResponseDTO(savedSlot);
    }

    @Override
    @CacheEvict(key = "#healthPackageId")
    public HealthPackageTimeSlotResponseDTO update(
            UUID healthPackageId,
            UUID slotId,
            UpdateHealthPackageTimeSlotRequestDTO requestDTO
    ) {
        HealthPackageTimeSlot slot = timeSlotRepository.findById(slotId)
                .filter(existing -> existing.getHealthPackage().getId().equals(healthPackageId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Time slot not found with id: " + slotId
                ));

        timeSlotMapper.updateEntity(requestDTO, slot);
        validateTimeSlot(healthPackageId, slotId, slot);

        HealthPackageTimeSlot updatedSlot = timeSlotRepository.save(slot);

        log.info("Updated time slot {} for health package {}", slotId, healthPackageId);

        return timeSlotMapper.toResponseDTO(updatedSlot);
    }

    @Override
    @CacheEvict(key = "#healthPackageId")
    public void delete(UUID healthPackageId, UUID slotId) {
        HealthPackageTimeSlot slot = timeSlotRepository.findById(slotId)
                .filter(existing -> existing.getHealthPackage().getId().equals(healthPackageId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Time slot not found with id: " + slotId
                ));

        timeSlotRepository.delete(slot);

        log.info("Deleted time slot {} for health package {}", slotId, healthPackageId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#healthPackageId")
    public List<HealthPackageTimeSlotResponseDTO> getAllByHealthPackage(UUID healthPackageId) {
        healthPackageRepository.findById(healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health package not found with id: " + healthPackageId
                ));

        return timeSlotRepository.findAllByHealthPackageId(healthPackageId)
                .stream()
                .map(timeSlotMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthPackageTimeSlotResponseDTO> getAvailableSlots(
            UUID healthPackageId,
            LocalDate appointmentDate
    ) {
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        HealthPackage healthPackage = healthPackageRepository.findByIdAndIsDeletedFalse(healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health package not found with id: " + healthPackageId
                ));

        availabilityValidator.validateHealthPackageWeeklySchedule(healthPackage, appointmentDate);
        availabilityValidator.validateAppointmentDateWithinAdvanceBookingLimit(healthPackage, appointmentDate);

        return timeSlotRepository.findAllByHealthPackageId(healthPackageId)
                .stream()
                .filter(slot -> {
                    long bookedCount = appointmentRepository.countActiveAppointmentsForSlot(
                            healthPackageId,
                            slot.getId(),
                            appointmentDate,
                            AppointmentStatus.CANCELLED
                    );

                    return bookedCount < slot.getTotalSlots();
                })
                .map(timeSlotMapper::toResponseDTO)
                .toList();
    }

    private void validateTimeSlot(
            UUID healthPackageId,
            UUID currentSlotId,
            HealthPackageTimeSlot slot
    ) {
        if (!slot.getStartTime().isBefore(slot.getEndTime())) {
            throw new ConflictException("Start time must be before end time");
        }

        boolean overlaps = currentSlotId == null
                ? timeSlotRepository.existsByHealthPackageIdAndStartTimeLessThanAndEndTimeGreaterThan(
                healthPackageId,
                slot.getEndTime(),
                slot.getStartTime()
        )
                : timeSlotRepository.existsByHealthPackageIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                healthPackageId,
                currentSlotId,
                slot.getEndTime(),
                slot.getStartTime()
        );

        if (overlaps) {
            throw new ConflictException("Time slot overlaps with an existing time slot");
        }
    }
}