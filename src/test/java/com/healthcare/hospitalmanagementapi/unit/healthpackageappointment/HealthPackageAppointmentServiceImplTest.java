package com.healthcare.hospitalmanagementapi.unit.healthpackageappointment;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.*;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.*;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageAppointmentMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.*;
import com.healthcare.hospitalmanagementapi.healthpackage.service.impl.*;
import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import com.healthcare.hospitalmanagementapi.patient.repository.PatientRepository;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthPackageAppointmentServiceImplTest {

    @Mock private HealthPackageAppointmentRepository appointmentRepository;
    @Mock private HealthPackageRepository healthPackageRepository;
    @Mock private HealthPackageTimeSlotRepository timeSlotRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private HealthPackageAppointmentMapper mapper;
    @Mock private HealthPackageAppointmentQueryService queryService;
    @Mock private HealthPackageAvailabilityValidator validator;

    @InjectMocks
    private HealthPackageAppointmentServiceImpl service;

    private UUID appointmentId;
    private UUID patientId;
    private UUID packageId;
    private UUID slotId;

    private Patient patient;
    private HealthPackage healthPackage;
    private HealthPackageTimeSlot slot;
    private HealthPackageAppointment appointment;
    private HealthPackageAppointmentResponseDTO response;

    @BeforeEach
    void setUp() {
        appointmentId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        packageId = UUID.randomUUID();
        slotId = UUID.randomUUID();

        patient = new Patient();
        patient.setId(patientId);

        healthPackage = new HealthPackage();
        healthPackage.setId(packageId);
        healthPackage.setIsActive(true);

        slot = new HealthPackageTimeSlot();
        slot.setId(slotId);
        slot.setHealthPackage(healthPackage);
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(11, 0));
        slot.setTotalSlots(5);

        appointment = new HealthPackageAppointment();
        appointment.setId(appointmentId);
        appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);

        response = HealthPackageAppointmentResponseDTO.builder()
                .id(appointmentId)
                .build();
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    /**
     * FIXED: lenient() to avoid UnnecessaryStubbingException
     */
    private void mockAuthUser() {
        User user = new User();
        user.setId(UUID.randomUUID());

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        lenient().when(userDetails.getUser()).thenReturn(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null)
        );
    }

    @Test
    void shouldCreateAppointment_whenValidInput() {
        mockAuthUser();

        CreateHealthPackageAppointmentRequestDTO request =
                CreateHealthPackageAppointmentRequestDTO.builder()
                        .patientId(patientId)
                        .healthPackageId(packageId)
                        .healthPackageTimeSlotId(slotId)
                        .appointmentDate(LocalDate.now().plusDays(1))
                        .build();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.of(patient));
        when(healthPackageRepository.findByIdAndIsDeletedFalse(packageId)).thenReturn(Optional.of(healthPackage));
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        when(appointmentRepository.countActiveAppointmentsForSlot(
                packageId, slotId, request.getAppointmentDate(), AppointmentStatus.CANCELLED
        )).thenReturn(1L);

        when(mapper.toEntity(request)).thenReturn(appointment);
        when(appointmentRepository.getNextTokenNumber(packageId, slotId, request.getAppointmentDate())).thenReturn(2);
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(mapper.toResponseDTO(appointment)).thenReturn(response);

        HealthPackageAppointmentResponseDTO result = service.createAppointment(request);

        assertThat(result).isEqualTo(response);

        ArgumentCaptor<HealthPackageAppointment> captor = ArgumentCaptor.forClass(HealthPackageAppointment.class);
        verify(appointmentRepository).save(captor.capture());

        HealthPackageAppointment saved = captor.getValue();

        assertThat(saved.getAppointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(saved.getTokenNumber()).isEqualTo(2);
        assertThat(saved.getAppointmentTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void shouldThrowException_whenPatientNotFound() {
        CreateHealthPackageAppointmentRequestDTO request =
                CreateHealthPackageAppointmentRequestDTO.builder()
                        .patientId(patientId)
                        .healthPackageId(packageId)
                        .healthPackageTimeSlotId(slotId)
                        .appointmentDate(LocalDate.now().plusDays(1))
                        .build();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAppointment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflict_whenSlotFull() {
        CreateHealthPackageAppointmentRequestDTO request =
                CreateHealthPackageAppointmentRequestDTO.builder()
                        .patientId(patientId)
                        .healthPackageId(packageId)
                        .healthPackageTimeSlotId(slotId)
                        .appointmentDate(LocalDate.now().plusDays(1))
                        .build();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId)).thenReturn(Optional.of(patient));
        when(healthPackageRepository.findByIdAndIsDeletedFalse(packageId)).thenReturn(Optional.of(healthPackage));
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        when(appointmentRepository.countActiveAppointmentsForSlot(
                packageId, slotId, request.getAppointmentDate(), AppointmentStatus.CANCELLED
        )).thenReturn(5L);

        assertThatThrownBy(() -> service.createAppointment(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Appointment limit reached for this time slot");
    }

    @Test
    void shouldReturnAppointment_whenGetById() {
        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId)).thenReturn(Optional.of(appointment));
        when(mapper.toResponseDTO(appointment)).thenReturn(response);

        HealthPackageAppointmentResponseDTO result = service.getAppointmentById(appointmentId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowException_whenAppointmentNotFound() {
        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAppointmentById(appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Health package appointment not found");
    }

    @Test
    void shouldUpdateAppointment_whenValidTransition() {
        UpdateHealthPackageAppointmentRequestDTO request =
                UpdateHealthPackageAppointmentRequestDTO.builder()
                        .appointmentStatus(AppointmentStatus.ADMITTED)
                        .build();

        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(mapper.toResponseDTO(appointment)).thenReturn(response);

        HealthPackageAppointmentResponseDTO result = service.updateAppointment(appointmentId, request);

        assertThat(result).isEqualTo(response);
        verify(mapper).updateEntityFromDto(request, appointment);
    }

    @Test
    void shouldThrowConflict_whenInvalidStatusTransition() {
        appointment.setAppointmentStatus(AppointmentStatus.ADMITTED);

        UpdateHealthPackageAppointmentRequestDTO request =
                UpdateHealthPackageAppointmentRequestDTO.builder()
                        .appointmentStatus(AppointmentStatus.CONFIRMED)
                        .build();

        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.updateAppointment(appointmentId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Invalid status transition from ADMITTED to CONFIRMED");
    }

    @Test
    void shouldDeleteAppointment_whenExists() {
        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId)).thenReturn(Optional.of(appointment));

        service.deleteAppointment(appointmentId);

        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void shouldRestoreAppointment_whenDeleted() {
        appointment.setIsDeleted(true);

        when(appointmentRepository.findByIdIncludingDeleted(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(mapper.toResponseDTO(appointment)).thenReturn(response);

        HealthPackageAppointmentResponseDTO result = service.restoreAppointment(appointmentId);

        assertThat(result).isEqualTo(response);
        assertThat(appointment.getIsDeleted()).isFalse();
        assertThat(appointment.getDeletedAt()).isNull();
    }
}