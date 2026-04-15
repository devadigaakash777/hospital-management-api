package com.healthcare.hospitalmanagementapi.doctor.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.CreateDoctorBlockedDateRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.DoctorBlockedDateResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorBlockedDate;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import com.healthcare.hospitalmanagementapi.doctor.mapper.DoctorBlockedDateMapper;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorBlockedDateRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorBlockedDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "doctorBlockedDates")
public class DoctorBlockedDateServiceImpl implements DoctorBlockedDateService {

    private final DoctorBlockedDateRepository doctorBlockedDateRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorBlockedDateMapper doctorBlockedDateMapper;
    private final DoctorTimeSlotRepository timeSlotRepository;

    private static final String BLOCKED_DATE_NOT_FOUND = "Blocked date not found";

    @Override
    @CachePut(key = "#result.id")
    public DoctorBlockedDateResponseDTO createBlockedDate(
            UUID doctorId,
            CreateDoctorBlockedDateRequestDTO dto
    ) {

        if (doctorBlockedDateRepository.existsByDoctor_IdAndBlockedDateAndIsDeletedFalse(
                doctorId,
                dto.getBlockedDate()
        )) {
            throw new ConflictException(
                    "Doctor already has a blocked date on " + dto.getBlockedDate()
            );
        }

        Doctor doctor = doctorRepository.findByIdAndIsDeletedFalse(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        DoctorBlockedDate blockedDate = doctorBlockedDateMapper.toEntity(dto);
        blockedDate.setDoctor(doctor);

        DoctorBlockedDate saved = doctorBlockedDateRepository.save(blockedDate);

        log.info(
                "Doctor blocked date created. doctorId={}, blockedDateId={}",
                doctorId,
                saved.getId()
        );

        return doctorBlockedDateMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#blockedDateId")
    public DoctorBlockedDateResponseDTO getBlockedDateById(UUID blockedDateId) {

        DoctorBlockedDate blockedDate = doctorBlockedDateRepository.findByIdAndIsDeletedFalse(blockedDateId)
                .orElseThrow(() -> new ResourceNotFoundException(BLOCKED_DATE_NOT_FOUND));

        return doctorBlockedDateMapper.toResponseDTO(blockedDate);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorBlockedDateResponseDTO> getBlockedDatesByDoctor(
            UUID doctorId,
            Pageable pageable
    ) {

        Page<DoctorBlockedDate> blockedDatePage =
                doctorBlockedDateRepository.findAllByDoctor_IdAndIsDeletedFalse(
                        doctorId,
                        pageable
                );

        Page<DoctorBlockedDateResponseDTO> dtoPage =
                blockedDatePage.map(doctorBlockedDateMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorBlockedDateResponseDTO> getBlockedDatesByDateRange(
            UUID doctorId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {

        if (startDate.isAfter(endDate)) {
            throw new ConflictException("Start date cannot be after end date");
        }

        Page<DoctorBlockedDate> blockedDatePage =
                doctorBlockedDateRepository.findAllByDoctor_IdAndBlockedDateBetweenAndIsDeletedFalse(
                        doctorId,
                        startDate,
                        endDate,
                        pageable
                );

        Page<DoctorBlockedDateResponseDTO> dtoPage =
                blockedDatePage.map(doctorBlockedDateMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @CacheEvict(key = "#blockedDateId")
    public void deleteBlockedDate(UUID blockedDateId) {

        DoctorBlockedDate blockedDate = doctorBlockedDateRepository
                .findByIdAndIsDeletedFalse(blockedDateId)
                .orElseThrow(() -> new ResourceNotFoundException(BLOCKED_DATE_NOT_FOUND));

        if (LocalDate.now().isEqual(blockedDate.getBlockedDate())) {

            LocalTime firstTimeSlotStartTime = timeSlotRepository
                    .findAllByDoctorId(blockedDate.getDoctor().getId())
                    .stream()
                    .map(DoctorTimeSlot::getStartTime)
                    .min(LocalTime::compareTo)
                    .orElse(null);

            if (firstTimeSlotStartTime != null
                    && !LocalTime.now().isBefore(firstTimeSlotStartTime)) {
                throw new ConflictException(
                        "Blocked date can only be deleted before the first doctor time slot starts"
                );
            }
        }

        doctorBlockedDateRepository.delete(blockedDate);

        log.info("Doctor blocked date deleted. blockedDateId={}", blockedDateId);
    }
}