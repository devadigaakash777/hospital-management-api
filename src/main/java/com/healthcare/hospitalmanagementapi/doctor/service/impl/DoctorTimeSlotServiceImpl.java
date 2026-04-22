package com.healthcare.hospitalmanagementapi.doctor.service.impl;

import com.healthcare.hospitalmanagementapi.appointment.repository.AppointmentRepository;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.CreateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.DoctorTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.UpdateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import com.healthcare.hospitalmanagementapi.doctor.mapper.DoctorTimeSlotMapper;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorBlockedTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorTimeSlotService;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import java.time.LocalDate;
import com.healthcare.hospitalmanagementapi.common.exception.custom.BadRequestException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "doctorTimeSlots")
public class DoctorTimeSlotServiceImpl implements DoctorTimeSlotService {

    private final DoctorAvailabilityValidator doctorAvailabilityValidator;
    private final DoctorRepository doctorRepository;
    private final DoctorTimeSlotRepository timeSlotRepository;
    private final DoctorTimeSlotMapper timeSlotMapper;
    private final AppointmentRepository appointmentRepository;
    private final DoctorBlockedTimeSlotRepository doctorBlockedTimeSlotRepository;

    @Override
    @CacheEvict(key = "#doctorId")
    public DoctorTimeSlotResponseDTO create(
            UUID doctorId,
            CreateDoctorTimeSlotRequestDTO requestDTO
    ) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        DoctorTimeSlot slot = timeSlotMapper.toEntity(requestDTO);
        validateTimeSlot(doctorId, null, slot);
        slot.setDoctor(doctor);
        slot.setPatientsPerSlot(slot.getTotalSlots() - slot.getReservedSlots());

        DoctorTimeSlot savedSlot = timeSlotRepository.save(slot);

        log.info("Created time slot {} for doctor {}", savedSlot.getId(), doctorId);

        return timeSlotMapper.toResponseDTO(savedSlot);
    }

    @Override
    @CacheEvict(key = "#doctorId")
    public DoctorTimeSlotResponseDTO update(
            UUID doctorId,
            UUID slotId,
            UpdateDoctorTimeSlotRequestDTO requestDTO
    ) {
        DoctorTimeSlot slot = timeSlotRepository.findById(slotId)
                .filter(existing -> existing.getDoctor().getId().equals(doctorId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Time slot not found with id: " + slotId
                ));

        timeSlotMapper.updateEntity(requestDTO, slot);

        validateTimeSlot(doctorId, slotId, slot);

        slot.setPatientsPerSlot(slot.getTotalSlots() - slot.getReservedSlots());

        DoctorTimeSlot updatedSlot = timeSlotRepository.save(slot);

        log.info("Updated time slot {} for doctor {}", slotId, doctorId);

        return timeSlotMapper.toResponseDTO(updatedSlot);
    }

    @Override
    @CacheEvict(key = "#doctorId")
    public void delete(UUID doctorId, UUID slotId) {
        DoctorTimeSlot slot = timeSlotRepository.findById(slotId)
                .filter(existing -> existing.getDoctor().getId().equals(doctorId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Time slot not found with id: " + slotId
                ));

        timeSlotRepository.delete(slot);

        log.info("Deleted time slot {} for doctor {}", slotId, doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#doctorId")
    public List<DoctorTimeSlotResponseDTO> getAllByDoctor(UUID doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        return timeSlotRepository.findAllByDoctorId(doctorId)
                .stream()
                .map(timeSlotMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorTimeSlotResponseDTO> getAvailableSlots(
            UUID doctorId,
            LocalDate appointmentDate,
            boolean isVip
    ) {

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        Doctor doctor = doctorRepository.findByIdAndIsDeletedFalse(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        doctorAvailabilityValidator.validateDoctorWeeklySchedule(doctor, appointmentDate);
        doctorAvailabilityValidator.validateDoctorBlockedDate(doctor, appointmentDate);
        doctorAvailabilityValidator.validateAppointmentDateWithinAdvanceBookingLimit(doctor, appointmentDate);

        List<DoctorTimeSlot> slots = timeSlotRepository.findAllByDoctorId(doctorId);

        return slots.stream()
                .filter(slot -> {
                    boolean isBlocked = doctorBlockedTimeSlotRepository
                            .existsByDoctor_IdAndBlockedDateAndStartTimeLessThanAndEndTimeGreaterThanAndIsDeletedFalse(
                                    doctorId,
                                    appointmentDate,
                                    slot.getEndTime(),
                                    slot.getStartTime()
                            );

                    if (isBlocked) return false;

                    long bookedCount = appointmentRepository.countActiveAppointmentsForSlot(
                            doctorId,
                            slot.getId(),
                            appointmentDate,
                            isVip,
                            AppointmentStatus.CANCELLED
                    );

                    int allowedCount = isVip
                            ? slot.getReservedSlots()
                            : slot.getPatientsPerSlot();

                    return bookedCount < allowedCount;
                })
                .map(timeSlotMapper::toResponseDTO)
                .toList();
    }

    private void validateTimeSlot(
            UUID doctorId,
            UUID currentSlotId,
            DoctorTimeSlot slot
    ) {
        if (!slot.getStartTime().isBefore(slot.getEndTime())) {
            throw new ConflictException("Start time must be before end time");
        }

        if (slot.getReservedSlots() > slot.getTotalSlots()) {
            throw new ConflictException("Reserved slots cannot be greater than total slots");
        }

        boolean overlaps = currentSlotId == null
                ? timeSlotRepository.existsByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(
                doctorId,
                slot.getEndTime(),
                slot.getStartTime()
        )
                : timeSlotRepository.existsByDoctorIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                doctorId,
                currentSlotId,
                slot.getEndTime(),
                slot.getStartTime()
        );

        if (overlaps) {
            throw new ConflictException("Time slot overlaps with an existing time slot");
        }
    }
}