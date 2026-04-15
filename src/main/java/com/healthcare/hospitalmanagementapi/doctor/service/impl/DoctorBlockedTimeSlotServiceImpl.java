package com.healthcare.hospitalmanagementapi.doctor.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.CreateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.DoctorBlockedTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.UpdateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorBlockedTimeSlot;
import com.healthcare.hospitalmanagementapi.doctor.mapper.DoctorBlockedTimeSlotMapper;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorBlockedTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorBlockedTimeSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
@CacheConfig(cacheNames = "doctorBlockedTimeSlots")
public class DoctorBlockedTimeSlotServiceImpl implements DoctorBlockedTimeSlotService {

    private final DoctorBlockedTimeSlotRepository doctorBlockedTimeSlotRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorBlockedTimeSlotMapper doctorBlockedTimeSlotMapper;

    private static final String BLOCKED_TIME_SLOT_NOT_FOUND = "Blocked time slot not found";

    @Override
    @CachePut(key = "#result.id")
    public DoctorBlockedTimeSlotResponseDTO createBlockedTimeSlot(
            UUID doctorId,
            CreateDoctorBlockedTimeSlotRequestDTO dto
    ) {

        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new ConflictException("Start time must be before end time");
        }

        boolean overlappingSlotExists = doctorBlockedTimeSlotRepository
                .existsByDoctor_IdAndBlockedDateAndStartTimeLessThanAndEndTimeGreaterThanAndIsDeletedFalse(
                        doctorId,
                        dto.getBlockedDate(),
                        dto.getEndTime(),
                        dto.getStartTime()
                );

        if (overlappingSlotExists) {
            throw new ConflictException(
                    "Blocked time slot overlaps with an existing blocked time slot"
            );
        }

        Doctor doctor = doctorRepository.findByIdAndIsDeletedFalse(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        DoctorBlockedTimeSlot blockedTimeSlot = doctorBlockedTimeSlotMapper.toEntity(dto);
        blockedTimeSlot.setDoctor(doctor);
        blockedTimeSlot.setBatchId(null);

        DoctorBlockedTimeSlot saved = doctorBlockedTimeSlotRepository.save(blockedTimeSlot);

        log.info(
                "Doctor blocked time slot created. doctorId={}, blockedTimeSlotId={}",
                doctorId,
                saved.getId()
        );

        return doctorBlockedTimeSlotMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#blockedTimeSlotId")
    public DoctorBlockedTimeSlotResponseDTO getBlockedTimeSlotById(UUID blockedTimeSlotId) {

        DoctorBlockedTimeSlot blockedTimeSlot = doctorBlockedTimeSlotRepository
                .findByIdAndIsDeletedFalse(blockedTimeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException(BLOCKED_TIME_SLOT_NOT_FOUND));

        return doctorBlockedTimeSlotMapper.toResponseDTO(blockedTimeSlot);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorBlockedTimeSlotResponseDTO> getBlockedTimeSlotsByDoctor(
            UUID doctorId,
            Pageable pageable
    ) {

        Page<DoctorBlockedTimeSlot> blockedTimeSlotPage =
                doctorBlockedTimeSlotRepository.findAllByDoctor_IdAndIsDeletedFalse(
                        doctorId,
                        pageable
                );

        Page<DoctorBlockedTimeSlotResponseDTO> dtoPage =
                blockedTimeSlotPage.map(doctorBlockedTimeSlotMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorBlockedTimeSlotResponseDTO> getBlockedTimeSlotsByDate(
            UUID doctorId,
            LocalDate blockedDate,
            Pageable pageable
    ) {

        Page<DoctorBlockedTimeSlot> blockedTimeSlotPage =
                doctorBlockedTimeSlotRepository
                        .findAllByDoctor_IdAndBlockedDateAndIsDeletedFalse(
                                doctorId,
                                blockedDate,
                                pageable
                        );

        Page<DoctorBlockedTimeSlotResponseDTO> dtoPage =
                blockedTimeSlotPage.map(doctorBlockedTimeSlotMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @CachePut(key = "#blockedTimeSlotId")
    public DoctorBlockedTimeSlotResponseDTO updateBlockedTimeSlot(
            UUID blockedTimeSlotId,
            UpdateDoctorBlockedTimeSlotRequestDTO requestDTO
    ) {

        DoctorBlockedTimeSlot blockedTimeSlot = doctorBlockedTimeSlotRepository
                .findByIdAndIsDeletedFalse(blockedTimeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException(BLOCKED_TIME_SLOT_NOT_FOUND));

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = getLocalTime(requestDTO, blockedTimeSlot, currentDate);


        if (requestDTO.getEndTime() != null && blockedTimeSlot.getBlockedDate().isEqual(currentDate)
                    && !requestDTO.getEndTime().isAfter(currentTime)) {
                throw new ConflictException(
                        "End time must be greater than current time"
                );
            }


        doctorBlockedTimeSlotMapper.updateEntityFromDto(requestDTO, blockedTimeSlot);

        if (!blockedTimeSlot.getStartTime().isBefore(blockedTimeSlot.getEndTime())) {
            throw new ConflictException("End time must be after start time");
        }

        DoctorBlockedTimeSlot updatedBlockedTimeSlot =
                doctorBlockedTimeSlotRepository.save(blockedTimeSlot);

        return doctorBlockedTimeSlotMapper.toResponseDTO(updatedBlockedTimeSlot);
    }

    private static @NotNull LocalTime getLocalTime(UpdateDoctorBlockedTimeSlotRequestDTO requestDTO, DoctorBlockedTimeSlot blockedTimeSlot, LocalDate currentDate) {
        LocalTime currentTime = LocalTime.now();

        if (blockedTimeSlot.getBlockedDate().isBefore(currentDate)) {
            throw new ConflictException("Past blocked time slot cannot be updated");
        }

        if (requestDTO.getStartTime() != null && blockedTimeSlot.getBlockedDate().isEqual(currentDate)
                    && !currentTime.isBefore(blockedTimeSlot.getStartTime())) {
                throw new ConflictException(
                        "Start time can only be updated before the blocked slot start time"
                );
            }
        return currentTime;
    }

    @Override
    @CacheEvict(key = "#blockedTimeSlotId")
    public void deleteBlockedTimeSlot(UUID blockedTimeSlotId) {

        DoctorBlockedTimeSlot blockedTimeSlot = doctorBlockedTimeSlotRepository
                .findByIdAndIsDeletedFalse(blockedTimeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException(BLOCKED_TIME_SLOT_NOT_FOUND));

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        if (blockedTimeSlot.getBlockedDate().isBefore(currentDate)) {
            throw new ConflictException("Past blocked time slot cannot be deleted");
        }

        if (blockedTimeSlot.getBlockedDate().isEqual(currentDate)
                && !currentTime.isBefore(blockedTimeSlot.getStartTime())) {
            throw new ConflictException(
                    "Blocked time slot can only be deleted before the blocked start time"
            );
        }

        doctorBlockedTimeSlotRepository.delete(blockedTimeSlot);

        log.info(
                "Doctor blocked time slot deleted. blockedTimeSlotId={}",
                blockedTimeSlotId
        );
    }
}