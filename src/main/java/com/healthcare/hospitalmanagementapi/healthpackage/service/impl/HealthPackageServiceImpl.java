package com.healthcare.hospitalmanagementapi.healthpackage.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.CreateHealthPackageRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageAvailabilityResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageShortResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.UpdateHealthPackageRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.HealthPackageWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageWeeklyScheduleMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageAppointmentRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageWeeklyScheduleRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "healthPackages")
public class HealthPackageServiceImpl implements HealthPackageService {

    private static final String HEALTH_PACKAGE_NOT_FOUND = "Health package not found";
    private static final String CREATED_AT = "createdAt";

    private final HealthPackageRepository healthPackageRepository;
    private final HealthPackageWeeklyScheduleRepository weeklyScheduleRepository;
    private final HealthPackageTimeSlotRepository timeSlotRepository;
    private final HealthPackageAppointmentRepository appointmentRepository;
    private final HealthPackageMapper healthPackageMapper;
    private final HealthPackageWeeklyScheduleMapper weeklyScheduleMapper;

    @Override
    @CachePut(key = "#result.id")
    public HealthPackageResponseDTO createHealthPackage(CreateHealthPackageRequestDTO dto) {

        HealthPackage healthPackage = healthPackageMapper.toEntity(dto);

        HealthPackage saved = healthPackageRepository.save(healthPackage);

        log.info("Health package created with id: {}", saved.getId());

        return healthPackageMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public HealthPackageResponseDTO getHealthPackageById(UUID id) {

        HealthPackage healthPackage = healthPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HEALTH_PACKAGE_NOT_FOUND));

        return healthPackageMapper.toResponseDTO(healthPackage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthPackageResponseDTO> getAllHealthPackages(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(CREATED_AT).descending());

        Page<HealthPackage> packagePage = healthPackageRepository.findAllByIsDeletedFalse(pageable);

        return PageResponse.of(packagePage.map(healthPackageMapper::toResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthPackageResponseDTO> getAllHealthPackagesIncludingDeleted(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "created_at")
        );

        Page<HealthPackage> packagePage = healthPackageRepository.findAllIncludingDeleted(pageable);

        return PageResponse.of(packagePage.map(healthPackageMapper::toResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthPackageShortResponseDTO> searchHealthPackages(
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(CREATED_AT).descending());

        Page<HealthPackage> packagePage = healthPackageRepository.searchHealthPackages(keyword, pageable);

        return PageResponse.of(packagePage.map(healthPackageMapper::toShortResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "healthPackageAvailability", key = "#healthPackageId")
    public HealthPackageAvailabilityResponseDTO getHealthPackageAvailability(UUID healthPackageId) {

        HealthPackage healthPackage = healthPackageRepository.findByIdAndIsDeletedFalse(healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(HEALTH_PACKAGE_NOT_FOUND));

        List<HealthPackageWeeklyScheduleResponseDTO> weeklySchedules =
                weeklyScheduleRepository.findAllByHealthPackageId(healthPackageId)
                        .stream()
                        .map(weeklyScheduleMapper::toResponseDTO)
                        .toList();

        return healthPackageMapper.toAvailabilityResponseDTO(healthPackage, weeklySchedules);
    }

    @Override
    @CachePut(key = "#id")
    public HealthPackageResponseDTO updateHealthPackage(UUID id, UpdateHealthPackageRequestDTO dto) {

        HealthPackage healthPackage = healthPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HEALTH_PACKAGE_NOT_FOUND));

        Integer previousAdvanceBookingDays = healthPackage.getAdvanceBookingDays();

        healthPackageMapper.updateEntity(dto, healthPackage);

        HealthPackage updated = healthPackageRepository.save(healthPackage);

        // If advance booking window was reduced, cancel future appointments now beyond the limit
        if (dto.getAdvanceBookingDays() != null
                && dto.getAdvanceBookingDays() < previousAdvanceBookingDays) {

            LocalDate lastAllowedDate = LocalDate.now()
                    .plusDays(dto.getAdvanceBookingDays());

            int cancelledCount = appointmentRepository.cancelAppointmentsBeyondAdvanceBookingLimit(
                    updated.getId(),
                    lastAllowedDate,
                    AppointmentStatus.CANCELLED,
                    Set.of(
                            AppointmentStatus.CANCELLED,
                            AppointmentStatus.COMPLETED
                    )
            );

            log.info(
                    "Cancelled {} health package appointments beyond advance booking limit for healthPackageId={}",
                    cancelledCount,
                    updated.getId()
            );
        }

        log.info("Health package updated with id: {}", id);

        return healthPackageMapper.toResponseDTO(updated);
    }

    @Override
    @CacheEvict(key = "#id")
    public void deleteHealthPackage(UUID id) {

        HealthPackage healthPackage = healthPackageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(HEALTH_PACKAGE_NOT_FOUND));

        weeklyScheduleRepository.deleteAll(
                weeklyScheduleRepository.findAllByHealthPackageId(id)
        );

        timeSlotRepository.deleteAll(
                timeSlotRepository.findAllByHealthPackageId(id)
        );

        healthPackageRepository.delete(healthPackage);

        log.info(
                "Health package and all related weekly schedules and time slots deleted with id: {}",
                id
        );
    }

    @Override
    @CachePut(key = "#id")
    public HealthPackageResponseDTO restoreHealthPackage(UUID id) {

        HealthPackage healthPackage = healthPackageRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(HEALTH_PACKAGE_NOT_FOUND));

        if (!Boolean.TRUE.equals(healthPackage.getIsDeleted())) {
            throw new ConflictException("Health package is already active and cannot be restored");
        }

        healthPackage.setIsDeleted(false);
        healthPackage.setDeletedAt(null);

        HealthPackage saved = healthPackageRepository.save(healthPackage);

        log.info("Health package restored with id: {}", id);

        return healthPackageMapper.toResponseDTO(saved);
    }
}