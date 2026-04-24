package com.healthcare.hospitalmanagementapi.unit.healthpackageschedule;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.*;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.*;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageWeeklyScheduleMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.*;
import com.healthcare.hospitalmanagementapi.healthpackage.service.impl.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthPackageWeeklyScheduleServiceImplTest {

    @Mock private HealthPackageRepository healthPackageRepository;
    @Mock private HealthPackageWeeklyScheduleRepository repository;
    @Mock private HealthPackageWeeklyScheduleMapper mapper;

    @InjectMocks
    private HealthPackageWeeklyScheduleServiceImpl service;

    private UUID packageId;
    private HealthPackage healthPackage;

    @BeforeEach
    void setUp() {
        packageId = UUID.randomUUID();
        healthPackage = new HealthPackage();
        healthPackage.setId(packageId);
    }

    private CreateHealthPackageWeeklyScheduleRequestDTO dto(int week, DayOfWeek day) {
        return CreateHealthPackageWeeklyScheduleRequestDTO.builder()
                .weekNumber(week)
                .dayOfWeek(day)
                .build();
    }

    // ---------------- BULK CREATE ----------------

    @Test
    void shouldCreateSchedules_whenValid() {
        List<CreateHealthPackageWeeklyScheduleRequestDTO> request =
                List.of(dto(1, DayOfWeek.MONDAY));

        HealthPackageWeeklySchedule entity = new HealthPackageWeeklySchedule();
        HealthPackageWeeklyScheduleResponseDTO response =
                HealthPackageWeeklyScheduleResponseDTO.builder().build();

        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));
        when(repository.existsByHealthPackageAndWeekNumberAndDayOfWeek(any(), any(), any()))
                .thenReturn(false);
        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.saveAll(any())).thenReturn(List.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        List<HealthPackageWeeklyScheduleResponseDTO> result =
                service.bulkCreate(packageId, request);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldThrowConflict_whenDuplicateInRequest() {
        List<CreateHealthPackageWeeklyScheduleRequestDTO> request =
                List.of(dto(1, DayOfWeek.MONDAY), dto(1, DayOfWeek.MONDAY));

        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));

        assertThatThrownBy(() -> service.bulkCreate(packageId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Duplicate schedule found in request");
    }

    @Test
    void shouldThrowConflict_whenAlreadyExistsInDB() {
        List<CreateHealthPackageWeeklyScheduleRequestDTO> request =
                List.of(dto(1, DayOfWeek.MONDAY));

        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));
        when(repository.existsByHealthPackageAndWeekNumberAndDayOfWeek(any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.bulkCreate(packageId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Schedule already exists");
    }

    // ---------------- BULK UPDATE ----------------

    @Test
    void shouldUpdateSchedules_whenValid() {
        List<CreateHealthPackageWeeklyScheduleRequestDTO> request =
                List.of(dto(2, DayOfWeek.TUESDAY));

        HealthPackageWeeklySchedule entity = new HealthPackageWeeklySchedule();

        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));
        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.saveAll(any())).thenReturn(List.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(HealthPackageWeeklyScheduleResponseDTO.builder().build());

        List<HealthPackageWeeklyScheduleResponseDTO> result =
                service.bulkUpdate(packageId, request);

        assertThat(result).hasSize(1);
        verify(repository).deleteAllByHealthPackageId(packageId);
    }

    @Test
    void shouldThrowConflict_whenDuplicateInBulkUpdate() {
        List<CreateHealthPackageWeeklyScheduleRequestDTO> request =
                List.of(dto(1, DayOfWeek.MONDAY), dto(1, DayOfWeek.MONDAY));

        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));

        assertThatThrownBy(() -> service.bulkUpdate(packageId, request))
                .isInstanceOf(ConflictException.class);
    }

    // ---------------- DELETE ----------------

    @Test
    void shouldDeleteSchedules_whenValid() {
        UUID scheduleId = UUID.randomUUID();

        HealthPackageWeeklySchedule entity = new HealthPackageWeeklySchedule();

        when(repository.findByIdAndHealthPackageId(scheduleId, packageId))
                .thenReturn(Optional.of(entity));

        service.bulkDelete(packageId, List.of(scheduleId));

        verify(repository).deleteAll(any());
    }

    @Test
    void shouldThrowException_whenScheduleNotFound_delete() {
        UUID scheduleId = UUID.randomUUID();

        when(repository.findByIdAndHealthPackageId(scheduleId, packageId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.bulkDelete(packageId, List.of(scheduleId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------- GET ----------------

    @Test
    void shouldGetAllSchedules() {
        when(healthPackageRepository.findById(packageId)).thenReturn(Optional.of(healthPackage));
        when(repository.findAllByHealthPackageId(packageId)).thenReturn(List.of(new HealthPackageWeeklySchedule()));
        when(mapper.toResponseDTO(any())).thenReturn(HealthPackageWeeklyScheduleResponseDTO.builder().build());

        List<HealthPackageWeeklyScheduleResponseDTO> result =
                service.getAllByHealthPackage(packageId);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetScheduleById() {
        UUID scheduleId = UUID.randomUUID();

        when(repository.findByIdAndHealthPackageId(scheduleId, packageId))
                .thenReturn(Optional.of(new HealthPackageWeeklySchedule()));
        when(mapper.toResponseDTO(any())).thenReturn(HealthPackageWeeklyScheduleResponseDTO.builder().build());

        HealthPackageWeeklyScheduleResponseDTO result =
                service.getById(packageId, scheduleId);

        assertThat(result).isNotNull();
    }
}