package com.healthcare.hospitalmanagementapi.unit.appointment;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentSearchRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.CreateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.UpdateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.entity.Appointment;
import com.healthcare.hospitalmanagementapi.appointment.mapper.AppointmentMapper;
import com.healthcare.hospitalmanagementapi.appointment.repository.AppointmentRepository;
import com.healthcare.hospitalmanagementapi.appointment.service.impl.AppointmentQueryService;
import com.healthcare.hospitalmanagementapi.appointment.service.impl.AppointmentServiceImpl;
import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.doctor.service.impl.DoctorAvailabilityValidator;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import com.healthcare.hospitalmanagementapi.patient.repository.PatientRepository;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private DoctorAvailabilityValidator doctorAvailabilityValidator;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorTimeSlotRepository doctorTimeSlotRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private AppointmentQueryService appointmentQueryService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private UUID doctorTimeSlotId;

    private Patient patient;
    private Doctor doctor;
    private DoctorTimeSlot doctorTimeSlot;
    private Appointment appointment;
    private CreateAppointmentRequestDTO createRequest;
    private AppointmentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        appointmentId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        doctorTimeSlotId = UUID.randomUUID();

        patient = new Patient();
        patient.setId(patientId);

        Department department = new Department();
        department.setDepartmentName("Cardiology");

        User doctorUser = new User();
        doctorUser.setFirstName("John");
        doctorUser.setLastName("Doe");

        doctor = Doctor.builder()
                .id(doctorId)
                .designation("Consultant")
                .specialization("Cardiology")
                .advanceBookingDays(30)
                .department(department)
                .user(doctorUser)
                .build();

        doctorTimeSlot = DoctorTimeSlot.builder()
                .id(doctorTimeSlotId)
                .doctor(doctor)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .patientsPerSlot(5)
                .reservedSlots(2)
                .build();

        appointment = Appointment.builder()
                .id(appointmentId)
                .patient(patient)
                .doctorId(doctorId)
                .doctorTimeSlot(doctorTimeSlot)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentStatus(AppointmentStatus.CONFIRMED)
                .isVip(false)
                .build();

        createRequest = CreateAppointmentRequestDTO.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .doctorTimeSlotId(doctorTimeSlotId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .patientMessage("Headache")
                .isVip(false)
                .build();

        responseDTO = AppointmentResponseDTO.builder()
                .id(appointmentId)
                .patientId(patientId)
                .doctorId(doctorId)
                .appointmentStatus(AppointmentStatus.CONFIRMED)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser() {
        User authenticatedUser = new User();
        authenticatedUser.setId(UUID.randomUUID());
        authenticatedUser.setFirstName("Staff");
        authenticatedUser.setLastName("User");

        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
        lenient().when(customUserDetails.getUser()).thenReturn(authenticatedUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customUserDetails, null)
        );
    }

    @Test
    void shouldCreateAppointment_whenValidInput() {
        mockAuthenticatedUser();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.of(doctorTimeSlot));

        when(appointmentRepository.countActiveAppointmentsForSlot(
                doctorId,
                doctorTimeSlotId,
                createRequest.getAppointmentDate(),
                false,
                AppointmentStatus.CANCELLED
        )).thenReturn(1L);

        when(appointmentMapper.toEntity(createRequest)).thenReturn(appointment);

        when(appointmentRepository.getNextTokenNumber(
                doctorId,
                doctorTimeSlotId,
                createRequest.getAppointmentDate()
        )).thenReturn(2);

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponseDTO(appointment)).thenReturn(responseDTO);

        AppointmentResponseDTO result = appointmentService.createAppointment(createRequest);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());

        Appointment savedAppointment = captor.getValue();

        assertThat(savedAppointment.getPatient()).isEqualTo(patient);
        assertThat(savedAppointment.getDoctorId()).isEqualTo(doctorId);
        assertThat(savedAppointment.getDoctorTimeSlot()).isEqualTo(doctorTimeSlot);
        assertThat(savedAppointment.getAppointmentStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(savedAppointment.getTokenNumber()).isEqualTo(2);
        assertThat(savedAppointment.getAppointmentTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(savedAppointment.getDoctorDesignationSnapshot()).isEqualTo("Consultant");
        assertThat(savedAppointment.getDoctorSpecializationSnapshot()).isEqualTo("Cardiology");
        assertThat(savedAppointment.getDepartmentNameSnapshot()).isEqualTo("Cardiology");

        verify(appointmentMapper).toResponseDTO(appointment);
    }

    @Test
    void shouldThrowException_whenPatientNotFound() {
        mockAuthenticatedUser();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found");

        verify(doctorRepository, never()).findByIdAndIsDeletedFalse(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenDoctorNotFound() {
        mockAuthenticatedUser();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found");

        verify(doctorTimeSlotRepository, never()).findById(any());
    }

    @Test
    void shouldThrowException_whenDoctorTimeSlotNotFound() {
        mockAuthenticatedUser();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor time slot not found");
    }

    @Test
    void shouldThrowConflictException_whenDoctorTimeSlotDoesNotBelongToDoctor() {
        mockAuthenticatedUser();

        Doctor anotherDoctor = Doctor.builder()
                .id(UUID.randomUUID())
                .build();

        doctorTimeSlot.setDoctor(anotherDoctor);

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.of(doctorTimeSlot));

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Doctor time slot does not belong to the selected doctor");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictException_whenAppointmentDateExceedsAdvanceBookingLimit() {
        mockAuthenticatedUser();

        createRequest.setAppointmentDate(LocalDate.now().plusDays(31));

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.of(doctorTimeSlot));

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Appointment can only be booked within 30 days");

        verify(doctorAvailabilityValidator, never())
                .validateDoctorWeeklySchedule(any(), any());
    }

    @Test
    void shouldThrowConflictException_whenDoctorWeeklyScheduleValidationFails() {
        mockAuthenticatedUser();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.of(doctorTimeSlot));

        doThrow(new ConflictException("Doctor is not available for selected day/week"))
                .when(doctorAvailabilityValidator)
                .validateDoctorWeeklySchedule(doctor, createRequest.getAppointmentDate());

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Doctor is not available for selected day/week");
    }

    @Test
    void shouldThrowConflictException_whenDoctorBlockedDateValidationFails() {
        mockAuthenticatedUser();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.of(doctorTimeSlot));

        doThrow(new ConflictException("Doctor is not available on selected date"))
                .when(doctorAvailabilityValidator)
                .validateDoctorBlockedDate(doctor, createRequest.getAppointmentDate());

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Doctor is not available on selected date");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictException_whenDoctorBlockedTimeSlotValidationFails() {
        mockAuthenticatedUser();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.of(doctorTimeSlot));

        doThrow(new ConflictException("Selected doctor time slot is blocked for the selected date"))
                .when(doctorAvailabilityValidator)
                .validateDoctorBlockedTimeSlot(
                        doctor,
                        doctorTimeSlot,
                        createRequest.getAppointmentDate()
                );

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Selected doctor time slot is blocked for the selected date");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictException_whenRegularSlotLimitReached() {
        mockAuthenticatedUser();

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.of(doctorTimeSlot));

        when(appointmentRepository.countActiveAppointmentsForSlot(
                doctorId,
                doctorTimeSlotId,
                createRequest.getAppointmentDate(),
                false,
                AppointmentStatus.CANCELLED
        )).thenReturn(5L);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Appointment limit reached for this time slot");
    }

    @Test
    void shouldThrowConflictException_whenVipSlotLimitReached() {
        mockAuthenticatedUser();

        createRequest.setIsVip(true);

        when(patientRepository.findByIdAndIsDeletedFalse(patientId))
                .thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));
        when(doctorTimeSlotRepository.findById(doctorTimeSlotId))
                .thenReturn(Optional.of(doctorTimeSlot));

        when(appointmentRepository.countActiveAppointmentsForSlot(
                doctorId,
                doctorTimeSlotId,
                createRequest.getAppointmentDate(),
                true,
                AppointmentStatus.CANCELLED
        )).thenReturn(2L);

        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("VIP appointment limit reached for this time slot");
    }

    @Test
    void shouldReturnAppointment_whenGetAppointmentById() {
        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponseDTO(appointment)).thenReturn(responseDTO);

        AppointmentResponseDTO result = appointmentService.getAppointmentById(appointmentId);

        assertThat(result).isEqualTo(responseDTO);

        verify(appointmentRepository).findByIdAndIsDeletedFalse(appointmentId);
        verify(appointmentMapper).toResponseDTO(appointment);
    }

    @Test
    void shouldThrowException_whenAppointmentNotFoundForGetById() {
        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found");
    }

    @Test
    void shouldReturnAppointmentsByDoctorId() {
        PageResponse<AppointmentResponseDTO> pageResponse = new PageResponse<>(
                new PageImpl<>(
                        java.util.List.of(responseDTO),
                        PageRequest.of(0, 10),
                        1
                )
        );

        when(appointmentQueryService.searchAppointments(any(AppointmentSearchRequestDTO.class), any()))
                .thenReturn(pageResponse);

        PageResponse<AppointmentResponseDTO> result = appointmentService.getAppointmentsByDoctorId(
                doctorId,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(appointmentId);
    }

    @Test
    void shouldUpdateAppointment_whenValidStatusTransition() {
        UpdateAppointmentRequestDTO request = UpdateAppointmentRequestDTO.builder()
                .appointmentStatus(AppointmentStatus.ADMITTED)
                .patientMessage("Updated message")
                .build();

        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toResponseDTO(appointment)).thenReturn(responseDTO);

        AppointmentResponseDTO result = appointmentService.updateAppointment(appointmentId, request);

        assertThat(result).isEqualTo(responseDTO);

        verify(appointmentMapper).updateEntity(request, appointment);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldThrowConflictException_whenUpdatingCancelledAppointment() {
        appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);

        UpdateAppointmentRequestDTO request = UpdateAppointmentRequestDTO.builder()
                .appointmentStatus(AppointmentStatus.COMPLETED)
                .build();

        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.updateAppointment(appointmentId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot update an appointment with status: CANCELLED");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictException_whenUpdatingCompletedAppointment() {
        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);

        UpdateAppointmentRequestDTO request = UpdateAppointmentRequestDTO.builder()
                .appointmentStatus(AppointmentStatus.CANCELLED)
                .build();

        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.updateAppointment(appointmentId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot update an appointment with status: COMPLETED");
    }

    @Test
    void shouldThrowConflictException_whenInvalidStatusTransition() {
        appointment.setAppointmentStatus(AppointmentStatus.ADMITTED);

        UpdateAppointmentRequestDTO request = UpdateAppointmentRequestDTO.builder()
                .appointmentStatus(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.updateAppointment(appointmentId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Invalid status transition from ADMITTED to CONFIRMED");
    }

    @Test
    void shouldUpdateAppointment_whenVipFlagChangedAndSlotAvailable() {
        UpdateAppointmentRequestDTO request = UpdateAppointmentRequestDTO.builder()
                .isVip(true)
                .build();

        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.of(appointment));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository.countActiveAppointmentsForSlotExcluding(
                doctorId,
                doctorTimeSlotId,
                appointment.getAppointmentDate(),
                true,
                AppointmentStatus.CANCELLED,
                appointmentId
        )).thenReturn(1L);

        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toResponseDTO(appointment)).thenReturn(responseDTO);

        AppointmentResponseDTO result = appointmentService.updateAppointment(appointmentId, request);

        assertThat(result).isEqualTo(responseDTO);

        verify(appointmentRepository).countActiveAppointmentsForSlotExcluding(
                doctorId,
                doctorTimeSlotId,
                appointment.getAppointmentDate(),
                true,
                AppointmentStatus.CANCELLED,
                appointmentId
        );
    }

    @Test
    void shouldThrowConflictException_whenVipSlotLimitReachedDuringUpdate() {
        UpdateAppointmentRequestDTO request = UpdateAppointmentRequestDTO.builder()
                .isVip(true)
                .build();

        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.of(appointment));
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));

        when(appointmentRepository.countActiveAppointmentsForSlotExcluding(
                doctorId,
                doctorTimeSlotId,
                appointment.getAppointmentDate(),
                true,
                AppointmentStatus.CANCELLED,
                appointmentId
        )).thenReturn(2L);

        assertThatThrownBy(() -> appointmentService.updateAppointment(appointmentId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("VIP appointment limit reached for this time slot");
    }

    @Test
    void shouldDeleteAppointment_whenAppointmentExists() {
        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.of(appointment));

        appointmentService.deleteAppointment(appointmentId);

        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void shouldThrowException_whenDeletingNonExistingAppointment() {
        when(appointmentRepository.findByIdAndIsDeletedFalse(appointmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.deleteAppointment(appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found");
    }

    @Test
    void shouldRestoreDeletedAppointment_whenAppointmentIsDeleted() {
        appointment.setIsDeleted(true);

        when(appointmentRepository.findByIdIncludingDeleted(appointmentId))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toResponseDTO(appointment)).thenReturn(responseDTO);

        AppointmentResponseDTO result = appointmentService.restoreAppointment(appointmentId);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(appointment.getIsDeleted()).isFalse();
        assertThat(appointment.getDeletedAt()).isNull();

        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldThrowConflictException_whenAppointmentAlreadyActiveDuringRestore() {
        appointment.setIsDeleted(false);

        when(appointmentRepository.findByIdIncludingDeleted(appointmentId))
                .thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.restoreAppointment(appointmentId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Appointment is already active");
    }

    @Test
    void shouldThrowException_whenRestoreAppointmentNotFound() {
        when(appointmentRepository.findByIdIncludingDeleted(appointmentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.restoreAppointment(appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found");
    }
}