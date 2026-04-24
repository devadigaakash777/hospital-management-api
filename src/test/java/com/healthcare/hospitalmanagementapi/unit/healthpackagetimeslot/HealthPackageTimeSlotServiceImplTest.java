package com.healthcare.hospitalmanagementapi.unit.healthpackagetimeslot;

import com.healthcare.hospitalmanagementapi.common.exception.custom.BadRequestException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.*;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.*;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageTimeSlotMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.*;
import com.healthcare.hospitalmanagementapi.healthpackage.service.impl.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthPackageTimeSlotServiceImplTest {

    @Mock private HealthPackageAvailabilityValidator validator;
    @Mock private HealthPackageRepository healthPackageRepository;
    @Mock private HealthPackageTimeSlotRepository timeSlotRepository;
    @Mock private HealthPackageTimeSlotMapper mapper;
    @Mock private HealthPackageAppointmentRepository appointmentRepository;

    @InjectMocks
    private HealthPackageTimeSlotServiceImpl service;

    private UUID packageId;
    private UUID slotId;

    private HealthPackage healthPackage;
    private HealthPackageTimeSlot slot;
    private HealthPackageTimeSlotResponseDTO response;

    @BeforeEach
    void setUp() {
        packageId = UUID.randomUUID();
        slotId = UUID.randomUUID();

        healthPackage = new HealthPackage();
        healthPackage.setId(packageId);

        slot = new HealthPackageTimeSlot();
        slot.setId(slotId);
        slot.setStartTime(LocalTime.of(10,0));
        slot.setEndTime(LocalTime.of(11,0));
        slot.setTotalSlots(5);
        slot.setHealthPackage(healthPackage);

        response = HealthPackageTimeSlotResponseDTO.builder()
                .id(slotId)
                .healthPackageId(packageId)
                .build();
    }

    // ---------------- CREATE ----------------

    @Test
    void shouldCreateTimeSlot_whenValidInput() {
        CreateHealthPackageTimeSlotRequestDTO request =
                CreateHealthPackageTimeSlotRequestDTO.builder()
                        .startTime(LocalTime.of(10,0))
                        .endTime(LocalTime.of(11,0))
                        .totalSlots(5)
                        .build();

        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));
        when(mapper.toEntity(request)).thenReturn(slot);
        when(timeSlotRepository.existsByHealthPackageIdAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(), any()))
                .thenReturn(false);
        when(timeSlotRepository.save(slot)).thenReturn(slot);
        when(mapper.toResponseDTO(slot)).thenReturn(response);

        HealthPackageTimeSlotResponseDTO result = service.create(packageId, request);

        assertThat(result).isEqualTo(response);
        verify(timeSlotRepository).save(slot);
    }

    @Test
    void shouldThrowException_whenHealthPackageNotFound_create() {
        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(packageId, CreateHealthPackageTimeSlotRequestDTO.builder().build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowConflict_whenTimeOverlap_create() {
        CreateHealthPackageTimeSlotRequestDTO request =
                CreateHealthPackageTimeSlotRequestDTO.builder()
                        .startTime(LocalTime.of(10,0))
                        .endTime(LocalTime.of(11,0))
                        .totalSlots(5)
                        .build();

        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));
        when(mapper.toEntity(request)).thenReturn(slot);
        when(timeSlotRepository.existsByHealthPackageIdAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(packageId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Time slot overlaps with an existing time slot");
    }

    // ---------------- UPDATE ----------------

    @Test
    void shouldUpdateTimeSlot_whenValid() {
        UpdateHealthPackageTimeSlotRequestDTO request =
                UpdateHealthPackageTimeSlotRequestDTO.builder()
                        .totalSlots(10)
                        .build();

        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(timeSlotRepository.existsByHealthPackageIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(), any(), any()))
                .thenReturn(false);
        when(timeSlotRepository.save(slot)).thenReturn(slot);
        when(mapper.toResponseDTO(slot)).thenReturn(response);

        HealthPackageTimeSlotResponseDTO result = service.update(packageId, slotId, request);

        assertThat(result).isEqualTo(response);
        verify(mapper).updateEntity(request, slot);
    }

    @Test
    void shouldThrowException_whenSlotNotFound_update() {
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(packageId, slotId, UpdateHealthPackageTimeSlotRequestDTO.builder().build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowConflict_whenInvalidTimeRange() {
        slot.setStartTime(LocalTime.of(11,0));
        slot.setEndTime(LocalTime.of(10,0));

        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> service.update(packageId, slotId, UpdateHealthPackageTimeSlotRequestDTO.builder().build()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start time must be before end time");
    }

    // ---------------- DELETE ----------------

    @Test
    void shouldDeleteTimeSlot_whenExists() {
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        service.delete(packageId, slotId);

        verify(timeSlotRepository).delete(slot);
    }

    // ---------------- GET ALL ----------------

    @Test
    void shouldGetAllSlots() {
        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));
        when(timeSlotRepository.findAllByHealthPackageId(packageId)).thenReturn(List.of(slot));
        when(mapper.toResponseDTO(slot)).thenReturn(response);

        List<HealthPackageTimeSlotResponseDTO> result = service.getAllByHealthPackage(packageId);

        assertThat(result).hasSize(1);
    }

    // ---------------- AVAILABLE ----------------

    @Test
    void shouldReturnAvailableSlots_whenNotFullyBooked() {
        LocalDate date = LocalDate.now().plusDays(1);

        when(healthPackageRepository.findByIdAndIsDeletedFalse(packageId)).thenReturn(Optional.of(healthPackage));
        doNothing().when(validator).validateHealthPackageWeeklySchedule(any(), any());
        doNothing().when(validator).validateAppointmentDateWithinAdvanceBookingLimit(any(), any());

        when(timeSlotRepository.findAllByHealthPackageId(packageId)).thenReturn(List.of(slot));
        when(appointmentRepository.countActiveAppointmentsForSlot(any(), any(), any(), any()))
                .thenReturn(1L);

        when(mapper.toResponseDTO(slot)).thenReturn(response);

        List<HealthPackageTimeSlotResponseDTO> result =
                service.getAvailableSlots(packageId, date);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFilterOutFullSlots() {
        LocalDate date = LocalDate.now().plusDays(1);

        when(healthPackageRepository.findByIdAndIsDeletedFalse(packageId)).thenReturn(Optional.of(healthPackage));
        doNothing().when(validator).validateHealthPackageWeeklySchedule(any(), any());
        doNothing().when(validator).validateAppointmentDateWithinAdvanceBookingLimit(any(), any());

        when(timeSlotRepository.findAllByHealthPackageId(packageId)).thenReturn(List.of(slot));
        when(appointmentRepository.countActiveAppointmentsForSlot(any(), any(), any(), any()))
                .thenReturn(5L); // full

        List<HealthPackageTimeSlotResponseDTO> result =
                service.getAvailableSlots(packageId, date);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowException_whenDateInPast() {
        LocalDate past = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> service.getAvailableSlots(packageId, past))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Appointment date cannot be in the past");
    }
}