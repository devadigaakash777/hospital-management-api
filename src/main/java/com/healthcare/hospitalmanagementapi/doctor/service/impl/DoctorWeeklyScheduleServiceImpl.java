package com.healthcare.hospitalmanagementapi.doctor.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.CreateDoctorWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.DoctorWeeklyScheduleResponseDTO;

import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorWeeklySchedule;
import com.healthcare.hospitalmanagementapi.doctor.mapper.DoctorWeeklyScheduleMapper;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorWeeklyScheduleRepository;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorWeeklyScheduleService;
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
@CacheConfig(cacheNames = "doctorWeeklySchedules")
public class DoctorWeeklyScheduleServiceImpl implements DoctorWeeklyScheduleService {

    private final DoctorRepository doctorRepository;
    private final DoctorWeeklyScheduleRepository weeklyScheduleRepository;
    private final DoctorWeeklyScheduleMapper weeklyScheduleMapper;
    private static final String SCHEDULE_NOT_FOUND = "Schedule not found with id: ";

    @Override
    @CachePut(key = "#doctorId")
    public List<DoctorWeeklyScheduleResponseDTO> bulkCreate(
            UUID doctorId,
            List<CreateDoctorWeeklyScheduleRequestDTO> requestDTOs
    ) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + doctorId
                ));

        Set<String> requestKeys = new HashSet<>();

        for (CreateDoctorWeeklyScheduleRequestDTO dto : requestDTOs) {
            String key = dto.getWeekNumber() + "-" + dto.getDayOfWeek();

            if (!requestKeys.add(key)) {
                throw new ConflictException(
                        "Duplicate schedule found in request for weekNumber " +
                                dto.getWeekNumber() +
                                " and day " +
                                dto.getDayOfWeek()
                );
            }

            boolean exists = weeklyScheduleRepository.existsByDoctorAndWeekNumberAndDayOfWeek(
                    doctor,
                    dto.getWeekNumber(),
                    dto.getDayOfWeek()
            );

            if (exists) {
                throw new ConflictException(
                        "Schedule already exists for weekNumber " +
                                dto.getWeekNumber() +
                                " and day " +
                                dto.getDayOfWeek()
                );
            }
        }

        List<DoctorWeeklySchedule> schedules = requestDTOs.stream()
                .map(dto -> {
                    DoctorWeeklySchedule schedule = weeklyScheduleMapper.toEntity(dto);
                    schedule.setDoctor(doctor);
                    return schedule;
                })
                .toList();

        return weeklyScheduleRepository.saveAll(schedules)
                .stream()
                .map(weeklyScheduleMapper::toResponseDTO)
                .toList();
    }

    @Override
    @CachePut(key = "#doctorId")
    public List<DoctorWeeklyScheduleResponseDTO> bulkUpdate(
            UUID doctorId,
            List<CreateDoctorWeeklyScheduleRequestDTO> requestDTOs
    ) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + doctorId
                ));

        Set<String> uniqueKeys = new HashSet<>();

        for (CreateDoctorWeeklyScheduleRequestDTO dto : requestDTOs) {
            String key = dto.getWeekNumber() + "-" + dto.getDayOfWeek();

            if (!uniqueKeys.add(key)) {
                throw new ConflictException(
                        "Duplicate schedule found for weekNumber " +
                                dto.getWeekNumber() +
                                " and day " +
                                dto.getDayOfWeek()
                );
            }
        }

        weeklyScheduleRepository.deleteAllByDoctorId(doctorId);
        weeklyScheduleRepository.flush();

        List<DoctorWeeklySchedule> schedules = requestDTOs.stream()
                .map(dto -> {
                    DoctorWeeklySchedule schedule = weeklyScheduleMapper.toEntity(dto);
                    schedule.setDoctor(doctor);
                    return schedule;
                })
                .toList();

        List<DoctorWeeklyScheduleResponseDTO> response = weeklyScheduleRepository.saveAll(schedules)
                .stream()
                .map(weeklyScheduleMapper::toResponseDTO)
                .toList();

        log.info("Replaced {} weekly schedules for doctor {}", response.size(), doctorId);

        return response;
    }

    @Override
    @CacheEvict(key = "#doctorId")
    public void bulkDelete(UUID doctorId, List<UUID> scheduleIds) {
        List<DoctorWeeklySchedule> schedules = scheduleIds.stream()
                .map(id -> weeklyScheduleRepository.findByIdAndDoctorId(id, doctorId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                SCHEDULE_NOT_FOUND + id
                        )))
                .toList();

        weeklyScheduleRepository.deleteAll(schedules);

        log.info("Deleted {} weekly schedules for doctor {}", schedules.size(), doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#doctorId")
    public List<DoctorWeeklyScheduleResponseDTO> getAllByDoctor(UUID doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        return weeklyScheduleRepository.findAllByDoctorId(doctorId)
                .stream()
                .map(weeklyScheduleMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#doctorId + '-' + #scheduleId")
    public DoctorWeeklyScheduleResponseDTO getById(UUID doctorId, UUID scheduleId) {
        DoctorWeeklySchedule schedule = weeklyScheduleRepository.findByIdAndDoctorId(scheduleId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        SCHEDULE_NOT_FOUND + scheduleId
                ));

        return weeklyScheduleMapper.toResponseDTO(schedule);
    }
}