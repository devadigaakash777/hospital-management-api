package com.healthcare.hospitalmanagementapi.healthpackage.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.CreateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.UpdateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageAppointment;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageTimeSlot;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageAppointmentMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageAppointmentRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.healthpackage.service.HealthPackageAppointmentService;
import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@CacheConfig(cacheNames = "healthPackageAppointments")
public class HealthPackageAppointmentServiceImpl implements HealthPackageAppointmentService {

    private static final String APPOINTMENT_NOT_FOUND = "Health package appointment not found";
    private static final String PATIENT_NOT_FOUND = "Patient not found";
    private static final String HEALTH_PACKAGE_NOT_FOUND = "Health package not found";
    private static final String TIME_SLOT_NOT_FOUND = "Health package time slot not found";

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

    private final HealthPackageAppointmentRepository appointmentRepository;
    private final HealthPackageRepository healthPackageRepository;
    private final HealthPackageTimeSlotRepository timeSlotRepository;
    private final PatientRepository patientRepository;
    private final HealthPackageAppointmentMapper appointmentMapper;
    private final HealthPackageAppointmentQueryService appointmentQueryService;
    private final HealthPackageAvailabilityValidator availabilityValidator;

    @Override
    @CachePut(key = "#result.id")
    public HealthPackageAppointmentResponseDTO createAppointment(
            CreateHealthPackageAppointmentRequestDTO request
    ) {
        Patient patient = getPatient(request.getPatientId());
        HealthPackage healthPackage = getHealthPackage(request.getHealthPackageId());
        validateHealthPackageIsActive(healthPackage);
        HealthPackageTimeSlot timeSlot = getTimeSlot(request.getHealthPackageTimeSlotId());

        validateTimeSlotBelongsToPackage(healthPackage, timeSlot);

        availabilityValidator.validateAppointmentDateNotInPast(request.getAppointmentDate());
        availabilityValidator.validateAppointmentDateWithinAdvanceBookingLimit(
                healthPackage,
                request.getAppointmentDate()
        );
        availabilityValidator.validateHealthPackageWeeklySchedule(
                healthPackage,
                request.getAppointmentDate()
        );

        validateAppointmentTimeNotPassed(request.getAppointmentDate(), timeSlot);

        validateSlotAvailability(healthPackage, timeSlot, request.getAppointmentDate());

        HealthPackageAppointment appointment = appointmentMapper.toEntity(request);

        appointment.setPatient(patient);
        appointment.setHealthPackage(healthPackage);
        appointment.setHealthPackageTimeSlot(timeSlot);
        appointment.setAppointmentTime(timeSlot.getStartTime());
        appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        appointment.setCreatedByUser(getAuthenticatedUser());

        appointment.setTokenNumber(
                appointmentRepository.getNextTokenNumber(
                        healthPackage.getId(),
                        timeSlot.getId(),
                        request.getAppointmentDate()
                )
        );

        HealthPackageAppointment saved = appointmentRepository.save(appointment);

        log.info(
                "Created health package appointment. appointmentId={}, patientId={}, healthPackageId={}, slotId={}, createdByUserId={}",
                saved.getId(),
                patient.getId(),
                healthPackage.getId(),
                timeSlot.getId(),
                getAuthenticatedUserIdForLogging()
        );

        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#appointmentId")
    public HealthPackageAppointmentResponseDTO getAppointmentById(UUID appointmentId) {
        return appointmentMapper.toResponseDTO(getActiveAppointment(appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthPackageAppointmentResponseDTO> getAllAppointments(Pageable pageable) {

        Page<HealthPackageAppointmentResponseDTO> page = appointmentRepository
                .findAllByIsDeletedFalseOrderByTokenNumberAsc(pageable)
                .map(appointmentMapper::toResponseDTO);

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthPackageAppointmentResponseDTO> getAllAppointmentsIncludingDeleted(
            Pageable pageable
    ) {
        Page<HealthPackageAppointmentResponseDTO> page = appointmentRepository
                .findAllIncludingDeleted(pageable)
                .map(appointmentMapper::toResponseDTO);

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthPackageAppointmentResponseDTO> getAppointmentsByHealthPackageId(
            UUID healthPackageId,
            Pageable pageable
    ) {
        HealthPackageAppointmentSearchRequestDTO request = HealthPackageAppointmentSearchRequestDTO
                .builder()
                .healthPackageId(healthPackageId)
                .build();

        return appointmentQueryService.searchAppointments(request, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthPackageAppointmentResponseDTO> getAppointmentsByHealthPackageTimeSlotId(
            UUID healthPackageTimeSlotId,
            Pageable pageable
    ) {
        Page<HealthPackageAppointmentResponseDTO> page = appointmentRepository
                .findByHealthPackageTimeSlotId(healthPackageTimeSlotId, pageable)
                .map(appointmentMapper::toResponseDTO);

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HealthPackageAppointmentResponseDTO> searchAppointments(
            HealthPackageAppointmentSearchRequestDTO request,
            Pageable pageable
    ) {
        return appointmentQueryService.searchAppointments(request, pageable);
    }

    @Override
    @CachePut(key = "#appointmentId")
    public HealthPackageAppointmentResponseDTO updateAppointment(
            UUID appointmentId,
            UpdateHealthPackageAppointmentRequestDTO request
    ) {
        HealthPackageAppointment appointment = getActiveAppointment(appointmentId);

        validateStatusTransition(appointment.getAppointmentStatus(), request.getAppointmentStatus());

        appointmentMapper.updateEntityFromDto(request, appointment);

        HealthPackageAppointment updated = appointmentRepository.save(appointment);

        log.info("Updated health package appointment. appointmentId={}", updated.getId());

        return appointmentMapper.toResponseDTO(updated);
    }

    @Override
    @CacheEvict(key = "#appointmentId")
    public void deleteAppointment(UUID appointmentId) {

        HealthPackageAppointment appointment = getActiveAppointment(appointmentId);

        appointmentRepository.delete(appointment);

        log.info("Deleted health package appointment. appointmentId={}", appointmentId);
    }

    @Override
    @CachePut(key = "#appointmentId")
    public HealthPackageAppointmentResponseDTO restoreAppointment(UUID appointmentId) {

        HealthPackageAppointment appointment = appointmentRepository
                .findByIdIncludingDeleted(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND));

        if (!Boolean.TRUE.equals(appointment.getIsDeleted())) {
            throw new ConflictException("Health package appointment is already active");
        }

        appointment.setIsDeleted(false);
        appointment.setDeletedAt(null);

        HealthPackageAppointment restored = appointmentRepository.save(appointment);

        log.info("Restored health package appointment. appointmentId={}", restored.getId());

        return appointmentMapper.toResponseDTO(restored);
    }

    // ─── private helpers ────────────────────────────────────────────────────────

    private Patient getPatient(UUID patientId) {
        return patientRepository.findByIdAndIsDeletedFalse(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(PATIENT_NOT_FOUND));
    }

    private HealthPackage getHealthPackage(UUID healthPackageId) {
        return healthPackageRepository.findByIdAndIsDeletedFalse(healthPackageId)
                .orElseThrow(() -> new ResourceNotFoundException(HEALTH_PACKAGE_NOT_FOUND));
    }

    private HealthPackageTimeSlot getTimeSlot(UUID timeSlotId) {
        return timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException(TIME_SLOT_NOT_FOUND));
    }

    private HealthPackageAppointment getActiveAppointment(UUID appointmentId) {
        return appointmentRepository.findByIdAndIsDeletedFalse(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND));
    }

    private void validateTimeSlotBelongsToPackage(
            HealthPackage healthPackage,
            HealthPackageTimeSlot timeSlot
    ) {
        if (!timeSlot.getHealthPackage().getId().equals(healthPackage.getId())) {
            throw new ConflictException(
                    "Health package time slot does not belong to the selected health package"
            );
        }
    }

    /**
     * Slot availability check during CREATION — counts all active appointments
     * for the slot and rejects if totalSlots is already reached.
     */
    private void validateSlotAvailability(
            HealthPackage healthPackage,
            HealthPackageTimeSlot timeSlot,
            LocalDate appointmentDate
    ) {
        long bookedCount = appointmentRepository.countActiveAppointmentsForSlot(
                healthPackage.getId(),
                timeSlot.getId(),
                appointmentDate,
                AppointmentStatus.CANCELLED
        );

        if (bookedCount >= timeSlot.getTotalSlots()) {
            throw new ConflictException("Appointment limit reached for this time slot");
        }
    }

    /**
     * Validates that same-day bookings are made within 15 minutes after
     * the slot's start time — mirrors AppointmentServiceImpl behaviour.
     */
    private void validateAppointmentTimeNotPassed(
            LocalDate appointmentDate,
            HealthPackageTimeSlot timeSlot
    ) {
        if (appointmentDate.equals(LocalDate.now())) {
            LocalTime currentTime = LocalTime.now();

            LocalTime cutoffTime = timeSlot.getEndTime().minusMinutes(15);

            if (!currentTime.isBefore(cutoffTime)) {
                throw new ConflictException(
                        "Booking is allowed only until 15 minutes before the slot end time"
                );
            }
        }
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

    /**
     * Resolves the authenticated user's ID purely for audit logging.
     * Returns "anonymous" if no authenticated principal is present.
     */
    private String getAuthenticatedUserIdForLogging() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(CustomUserDetails.class::isInstance)
                .map(p -> ((CustomUserDetails) p).getUser().getId().toString())
                .orElse("anonymous");
    }

    private void validateHealthPackageIsActive(HealthPackage healthPackage) {
        if (!Boolean.TRUE.equals(healthPackage.getIsActive())) {
            throw new ConflictException("Health package is not active");
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ConflictException("Authenticated user not found");
        }

        return userDetails.getUser();
    }
}