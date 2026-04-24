package com.healthcare.hospitalmanagementapi.appointment.service.impl;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.CreateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.UpdateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.entity.Appointment;
import com.healthcare.hospitalmanagementapi.appointment.mapper.AppointmentMapper;
import com.healthcare.hospitalmanagementapi.appointment.repository.AppointmentRepository;
import com.healthcare.hospitalmanagementapi.appointment.service.AppointmentService;
import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import com.healthcare.hospitalmanagementapi.doctor.repository.*;
import com.healthcare.hospitalmanagementapi.doctor.service.impl.DoctorAvailabilityValidator;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import com.healthcare.hospitalmanagementapi.patient.repository.PatientRepository;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@CacheConfig(cacheNames = "appointments")
public class AppointmentServiceImpl implements AppointmentService {



    private static final String APPOINTMENT_NOT_FOUND_MESSAGE = "Appointment not found";
    private static final String PATIENT_NOT_FOUND_MESSAGE = "Patient not found";
    private static final String DOCTOR_NOT_FOUND_MESSAGE = "Doctor not found";
    private static final String DOCTOR_TIME_SLOT_NOT_FOUND_MESSAGE = "Doctor time slot not found";

    private static final Set<AppointmentStatus> IMMUTABLE_STATUSES = Set.of(
            AppointmentStatus.CANCELLED,
            AppointmentStatus.COMPLETED
    );

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            AppointmentStatus.CONFIRMED, Set.of(
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.ADMITTED,
                    AppointmentStatus.COMPLETED
            ),
            AppointmentStatus.ADMITTED, Set.of(
                    AppointmentStatus.COMPLETED,
                    AppointmentStatus.CANCELLED
            )
    );

    private final DoctorAvailabilityValidator doctorAvailabilityValidator;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorTimeSlotRepository doctorTimeSlotRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentQueryService appointmentQueryService;


    @Override
    @CachePut(key = "#result.id")
    public AppointmentResponseDTO createAppointment(CreateAppointmentRequestDTO request) {

        Patient patient = getPatient(request.getPatientId());
        Doctor doctor = getDoctor(request.getDoctorId());
        DoctorTimeSlot doctorTimeSlot = getDoctorTimeSlot(request.getDoctorTimeSlotId());

        validateDoctorTimeSlotBelongsToDoctor(doctor, doctorTimeSlot);
        validateAdvanceBookingLimit(doctor, request.getAppointmentDate());

        validateDoctorAvailabilityForDateAndSlot(
                doctor,
                doctorTimeSlot,
                request.getAppointmentDate()
        );

        validateAppointmentTimeNotPassed(
                request.getAppointmentDate(),
                doctorTimeSlot
        );

        boolean isVip = Boolean.TRUE.equals(request.getIsVip());

        validateSlotAvailability(
                doctor,
                doctorTimeSlot,
                request.getAppointmentDate(),
                isVip
        );

        Appointment appointment = appointmentMapper.toEntity(request);

        appointment.setPatient(patient);
        appointment.setDoctorId(doctor.getId());
        appointment.setDoctorTimeSlot(doctorTimeSlot);
        appointment.setAppointmentTime(doctorTimeSlot.getStartTime());
        appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        appointment.setCreatedByUser(getAuthenticatedUser());
        appointment.setIsVip(isVip);

        appointment.setTokenNumber(
                appointmentRepository.getNextTokenNumber(
                        doctor.getId(),
                        doctorTimeSlot.getId(),
                        request.getAppointmentDate()
                )
        );

        setDoctorSnapshotDetails(appointment, doctor);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        log.info(
                "Created appointment. appointmentId={}, patientId={}, doctorId={}, slotId={}",
                savedAppointment.getId(),
                patient.getId(),
                doctor.getId(),
                doctorTimeSlot.getId()
        );

        return appointmentMapper.toResponseDTO(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#appointmentId")
    public AppointmentResponseDTO getAppointmentById(UUID appointmentId) {
        return appointmentMapper.toResponseDTO(getActiveAppointment(appointmentId));
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getAppointmentsByDoctorTimeSlotId(
            UUID doctorTimeSlotId,
            Pageable pageable
    ) {
        Page<AppointmentResponseDTO> page = appointmentRepository
                .findByDoctorTimeSlotId(doctorTimeSlotId, pageable)
                .map(appointmentMapper::toResponseDTO);

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getAllAppointments(Pageable pageable) {

        Page<AppointmentResponseDTO> page = appointmentRepository
                .findAllByIsDeletedFalseOrderByIsVipDescTokenNumberAsc(pageable)
                .map(appointmentMapper::toResponseDTO);

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getAllAppointmentsIncludingDeleted(Pageable pageable) {

        Page<AppointmentResponseDTO> page = appointmentRepository
                .findAllIncludingDeleted(pageable)
                .map(appointmentMapper::toResponseDTO);

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getAppointmentsByDoctorId(
            UUID doctorId,
            Pageable pageable
    ) {
        AppointmentSearchRequestDTO request = AppointmentSearchRequestDTO.builder()
                .doctorId(doctorId)
                .build();

        return appointmentQueryService.searchAppointments(request, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> getAppointmentsByCreatedUserId(
            UUID createdByUserId,
            Pageable pageable
    ) {
        AppointmentSearchRequestDTO request = AppointmentSearchRequestDTO.builder()
                .createdByUserId(createdByUserId)
                .build();

        return appointmentQueryService.searchAppointments(request, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponseDTO> searchAppointments(
            AppointmentSearchRequestDTO request,
            Pageable pageable
    ) {
        return appointmentQueryService.searchAppointments(request, pageable);
    }

    @Override
    @CachePut(key = "#appointmentId")
    public AppointmentResponseDTO updateAppointment(
            UUID appointmentId,
            UpdateAppointmentRequestDTO request
    ) {
        Appointment appointment = getActiveAppointment(appointmentId);

        validateStatusTransition(appointment.getAppointmentStatus(), request.getAppointmentStatus());

        Boolean newIsVip = request.getIsVip() != null
                ? request.getIsVip()
                : appointment.getIsVip();

        if (!Objects.equals(newIsVip, appointment.getIsVip())) {
            Doctor doctor = getDoctor(appointment.getDoctorId());

            validateSlotAvailabilityExcluding(
                    doctor,
                    appointment.getDoctorTimeSlot(),
                    appointment.getAppointmentDate(),
                    newIsVip,
                    appointment.getId()
            );
        }

        appointmentMapper.updateEntity(request, appointment);

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        log.info("Updated appointment. appointmentId={}", updatedAppointment.getId());

        return appointmentMapper.toResponseDTO(updatedAppointment);
    }


    @Override
    @CacheEvict(key = "#appointmentId")
    public void deleteAppointment(UUID appointmentId) {

        Appointment appointment = getActiveAppointment(appointmentId);

        appointmentRepository.delete(appointment);

        log.info("Deleted appointment. appointmentId={}", appointmentId);
    }

    @Override
    @CachePut(key = "#appointmentId")
    public AppointmentResponseDTO restoreAppointment(UUID appointmentId) {

        Appointment appointment = appointmentRepository.findByIdIncludingDeleted(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE));

        if (!Boolean.TRUE.equals(appointment.getIsDeleted())) {
            throw new ConflictException("Appointment is already active");
        }

        appointment.setIsDeleted(false);
        appointment.setDeletedAt(null);

        Appointment restoredAppointment = appointmentRepository.save(appointment);

        log.info("Restored appointment. appointmentId={}", restoredAppointment.getId());

        return appointmentMapper.toResponseDTO(restoredAppointment);
    }

    private Patient getPatient(UUID patientId) {
        return patientRepository.findByIdAndIsDeletedFalse(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(PATIENT_NOT_FOUND_MESSAGE));
    }

    private Doctor getDoctor(UUID doctorId) {
        return doctorRepository.findByIdAndIsDeletedFalse(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MESSAGE));
    }

    private DoctorTimeSlot getDoctorTimeSlot(UUID doctorTimeSlotId) {
        return doctorTimeSlotRepository.findById(doctorTimeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_TIME_SLOT_NOT_FOUND_MESSAGE));
    }

    private Appointment getActiveAppointment(UUID appointmentId) {
        return appointmentRepository.findByIdAndIsDeletedFalse(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE));
    }

    /**
     * Rejects updates when the appointment is already in an immutable status
     * (CANCELLED or COMPLETED), and validates that the requested transition is
     * permitted by {@link #ALLOWED_TRANSITIONS}.
     */
    private void validateStatusTransition(
            AppointmentStatus current,
            AppointmentStatus requested
    ) {
        if (IMMUTABLE_STATUSES.contains(current)) {
            throw new ConflictException(
                    "Cannot update an appointment with status: " + current
            );
        }

        if (requested == null || requested.equals(current)) {
            return; // no status change requested — nothing to validate
        }

        Set<AppointmentStatus> validNext = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());

        if (!validNext.contains(requested)) {
            throw new ConflictException(
                    "Invalid status transition from " + current + " to " + requested
            );
        }
    }

    private void validateDoctorTimeSlotBelongsToDoctor(
            Doctor doctor,
            DoctorTimeSlot doctorTimeSlot
    ) {
        if (!doctorTimeSlot.getDoctor().getId().equals(doctor.getId())) {
            throw new ConflictException(
                    "Doctor time slot does not belong to the selected doctor"
            );
        }
    }

    private void validateAdvanceBookingLimit(
            Doctor doctor,
            LocalDate appointmentDate
    ) {
        LocalDate maxAllowedDate = LocalDate.now()
                .plusDays(doctor.getAdvanceBookingDays());

        if (appointmentDate.isAfter(maxAllowedDate)) {
            throw new ConflictException(
                    "Appointment can only be booked within "
                            + doctor.getAdvanceBookingDays()
                            + " days"
            );
        }
    }

    /**
     * Slot availability check used during CREATION — counts all active
     * appointments for the bucket without any exclusion.
     */
    private void validateSlotAvailability(
            Doctor doctor,
            DoctorTimeSlot doctorTimeSlot,
            LocalDate appointmentDate,
            boolean isVip
    ) {
        long bookedCount = appointmentRepository.countActiveAppointmentsForSlot(
                doctor.getId(),
                doctorTimeSlot.getId(),
                appointmentDate,
                isVip,
                AppointmentStatus.CANCELLED
        );

        checkSlotLimit(bookedCount, doctorTimeSlot, isVip);
    }

    /**
     * Slot availability check used during UPDATE — excludes the appointment
     * being updated so it does not count against the new bucket's limit.
     */
    private void validateSlotAvailabilityExcluding(
            Doctor doctor,
            DoctorTimeSlot doctorTimeSlot,
            LocalDate appointmentDate,
            boolean isVip,
            UUID excludeAppointmentId
    ) {
        long bookedCount = appointmentRepository.countActiveAppointmentsForSlotExcluding(
                doctor.getId(),
                doctorTimeSlot.getId(),
                appointmentDate,
                isVip,
                AppointmentStatus.CANCELLED,
                excludeAppointmentId
        );

        checkSlotLimit(bookedCount, doctorTimeSlot, isVip);
    }

    /**
     * Shared limit check used by both slot-availability validators.
     */
    private void checkSlotLimit(
            long bookedCount,
            DoctorTimeSlot doctorTimeSlot,
            boolean isVip
    ) {
        int slotLimit = isVip
                ? doctorTimeSlot.getReservedSlots()
                : doctorTimeSlot.getPatientsPerSlot();

        if (bookedCount >= slotLimit) {
            throw new ConflictException(
                    isVip
                            ? "VIP appointment limit reached for this time slot"
                            : "Appointment limit reached for this time slot"
            );
        }
    }

    private void setDoctorSnapshotDetails(
            Appointment appointment,
            Doctor doctor
    ) {
        appointment.setDoctorDesignationSnapshot(doctor.getDesignation());
        appointment.setDoctorSpecializationSnapshot(doctor.getSpecialization());
        appointment.setDepartmentNameSnapshot(
                doctor.getDepartment().getDepartmentName()
        );
    }

    private User getAuthenticatedUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ConflictException("Authenticated user not found");
        }

        return userDetails.getUser();
    }

    private void validateAppointmentTimeNotPassed(
            LocalDate appointmentDate,
            DoctorTimeSlot doctorTimeSlot
    ) {
        if (appointmentDate.equals(LocalDate.now())) {
            LocalTime currentTime = LocalTime.now();

            LocalTime cutoffTime = doctorTimeSlot.getEndTime().minusMinutes(15);

            if (cutoffTime.isBefore(doctorTimeSlot.getStartTime())) {
                cutoffTime = doctorTimeSlot.getStartTime();
            }

            if (!currentTime.isBefore(cutoffTime)) {
                throw new ConflictException(
                        "Booking is allowed only until 15 minutes before the slot end time"
                );
            }
        }
    }

    private void validateDoctorAvailabilityForDateAndSlot(
            Doctor doctor,
            DoctorTimeSlot doctorTimeSlot,
            LocalDate appointmentDate
    ) {
        doctorAvailabilityValidator.validateAppointmentDateNotInPast(appointmentDate);
        doctorAvailabilityValidator.validateAppointmentDateWithinAdvanceBookingLimit(doctor, appointmentDate);
        doctorAvailabilityValidator.validateDoctorWeeklySchedule(doctor, appointmentDate);
        doctorAvailabilityValidator.validateDoctorBlockedDate(doctor, appointmentDate);
        doctorAvailabilityValidator.validateDoctorBlockedTimeSlot(doctor, doctorTimeSlot, appointmentDate);
    }
}