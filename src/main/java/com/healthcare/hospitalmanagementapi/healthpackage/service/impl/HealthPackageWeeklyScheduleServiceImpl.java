package com.healthcare.hospitalmanagementapi.healthpackage.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.CreateHealthPackageWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.HealthPackageWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageWeeklySchedule;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageWeeklyScheduleMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageWeeklyScheduleRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageWeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "healthPackageWeeklySchedules")
public class HealthPackageWeeklyScheduleServiceImpl implements HealthPackageWeeklyScheduleService {

    private final HealthPackageRepository healthPackageRepository;
    private final HealthPackageWeeklyScheduleRepository weeklyScheduleRepository;
    private final HealthPackageWeeklyScheduleMapper weeklyScheduleMapper;

    private static final String SCHEDULE_NOT_FOUND = "Schedule not found with id: ";

    @Override
    @CachePut(key = "#healthPackageId")
    public List<HealthPackageWeeklyScheduleResponseDTO> bulkCreate(
            UUID healthPackageId,
            List<CreateHealthPackageWeeklyScheduleRequestDTO> requestDTOs
    ) {
        HealthPackage healthPackage = healthPackageRepository.findById(healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health package not found with id: " + healthPackageId
                ));

        Set<String> requestKeys = new HashSet<>();

        for (CreateHealthPackageWeeklyScheduleRequestDTO dto : requestDTOs) {
            String key = dto.getWeekNumber() + "-" + dto.getDayOfWeek();

            if (!requestKeys.add(key)) {
                throw new ConflictException(
                        "Duplicate schedule found in request for weekNumber "
                                + dto.getWeekNumber()
                                + " and day "
                                + dto.getDayOfWeek()
                );
            }

            boolean exists = weeklyScheduleRepository.existsByHealthPackageAndWeekNumberAndDayOfWeek(
                    healthPackage,
                    dto.getWeekNumber(),
                    dto.getDayOfWeek()
            );

            if (exists) {
                throw new ConflictException(
                        "Schedule already exists for weekNumber "
                                + dto.getWeekNumber()
                                + " and day "
                                + dto.getDayOfWeek()
                );
            }
        }

        List<HealthPackageWeeklySchedule> schedules = requestDTOs.stream()
                .map(dto -> {
                    HealthPackageWeeklySchedule schedule = weeklyScheduleMapper.toEntity(dto);
                    schedule.setHealthPackage(healthPackage);
                    return schedule;
                })
                .toList();

        return weeklyScheduleRepository.saveAll(schedules)
                .stream()
                .map(weeklyScheduleMapper::toResponseDTO)
                .toList();
    }

    @Override
    @CachePut(key = "#healthPackageId")
    public List<HealthPackageWeeklyScheduleResponseDTO> bulkUpdate(
            UUID healthPackageId,
            List<CreateHealthPackageWeeklyScheduleRequestDTO> requestDTOs
    ) {
        HealthPackage healthPackage = healthPackageRepository.findById(healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health package not found with id: " + healthPackageId
                ));

        Set<String> uniqueKeys = new HashSet<>();

        for (CreateHealthPackageWeeklyScheduleRequestDTO dto : requestDTOs) {
            String key = dto.getWeekNumber() + "-" + dto.getDayOfWeek();

            if (!uniqueKeys.add(key)) {
                throw new ConflictException(
                        "Duplicate schedule found for weekNumber "
                                + dto.getWeekNumber()
                                + " and day "
                                + dto.getDayOfWeek()
                );
            }
        }

        weeklyScheduleRepository.deleteAllByHealthPackageId(healthPackageId);
        weeklyScheduleRepository.flush();

        List<HealthPackageWeeklySchedule> schedules = requestDTOs.stream()
                .map(dto -> {
                    HealthPackageWeeklySchedule schedule = weeklyScheduleMapper.toEntity(dto);
                    schedule.setHealthPackage(healthPackage);
                    return schedule;
                })
                .toList();

        List<HealthPackageWeeklyScheduleResponseDTO> response = weeklyScheduleRepository.saveAll(schedules)
                .stream()
                .map(weeklyScheduleMapper::toResponseDTO)
                .toList();

        log.info("Replaced {} weekly schedules for health package {}", response.size(), healthPackageId);

        return response;
    }

    @Override
    @CacheEvict(key = "#healthPackageId")
    public void bulkDelete(UUID healthPackageId, List<UUID> scheduleIds) {
        List<HealthPackageWeeklySchedule> schedules = scheduleIds.stream()
                .map(id -> weeklyScheduleRepository.findByIdAndHealthPackageId(id, healthPackageId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                SCHEDULE_NOT_FOUND + id
                        )))
                .toList();

        weeklyScheduleRepository.deleteAll(schedules);

        log.info("Deleted {} weekly schedules for health package {}", schedules.size(), healthPackageId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#healthPackageId")
    public List<HealthPackageWeeklyScheduleResponseDTO> getAllByHealthPackage(UUID healthPackageId) {
        healthPackageRepository.findById(healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health package not found with id: " + healthPackageId
                ));

        return weeklyScheduleRepository.findAllByHealthPackageId(healthPackageId)
                .stream()
                .map(weeklyScheduleMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#healthPackageId + '-' + #scheduleId")
    public HealthPackageWeeklyScheduleResponseDTO getById(UUID healthPackageId, UUID scheduleId) {
        HealthPackageWeeklySchedule schedule = weeklyScheduleRepository
                .findByIdAndHealthPackageId(scheduleId, healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        SCHEDULE_NOT_FOUND + scheduleId
                ));

        return weeklyScheduleMapper.toResponseDTO(schedule);
    }
}