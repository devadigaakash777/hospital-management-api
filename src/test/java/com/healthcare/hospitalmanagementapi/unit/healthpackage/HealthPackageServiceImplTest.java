package com.healthcare.hospitalmanagementapi.unit.healthpackage;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.*;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.mapper.HealthPackageWeeklyScheduleMapper;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.*;

import com.healthcare.hospitalmanagementapi.healthpackage.service.impl.HealthPackageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthPackageServiceImplTest {

    @Mock private HealthPackageRepository repository;
    @Mock private HealthPackageWeeklyScheduleRepository weeklyRepository;
    @Mock private HealthPackageTimeSlotRepository slotRepository;
    @Mock private HealthPackageAppointmentRepository appointmentRepository;
    @Mock private HealthPackageMapper mapper;
    @Mock private HealthPackageWeeklyScheduleMapper weeklyMapper;

    @InjectMocks
    private HealthPackageServiceImpl service;

    private UUID id;
    private HealthPackage entity;
    private HealthPackageResponseDTO response;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        entity = new HealthPackage();
        entity.setId(id);
        entity.setAdvanceBookingDays(30);

        response = HealthPackageResponseDTO.builder()
                .id(id)
                .packageName("Test Package")
                .build();
    }

    @Test
    void shouldCreateHealthPackage_whenValidInput() {
        CreateHealthPackageRequestDTO request = CreateHealthPackageRequestDTO.builder()
                .packageName("Test")
                .advanceBookingDays(30)
                .packagePrice(java.math.BigDecimal.TEN)
                .build();

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        HealthPackageResponseDTO result = service.createHealthPackage(request);

        assertThat(result).isEqualTo(response);

        verify(repository).save(entity);
        verify(mapper).toResponseDTO(entity);
    }

    @Test
    void shouldReturnHealthPackage_whenExists() {
        when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        HealthPackageResponseDTO result = service.getHealthPackageById(id);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowException_whenHealthPackageNotFound() {
        when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHealthPackageById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Health package not found");
    }

    @Test
    void shouldReturnPagedHealthPackages() {
        Page<HealthPackage> page = new PageImpl<>(List.of(entity));

        when(repository.findAllByIsDeletedFalse(any())).thenReturn(page);
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        PageResponse<HealthPackageResponseDTO> result = service.getAllHealthPackages(0,10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldSearchHealthPackages() {
        Page<HealthPackage> page = new PageImpl<>(List.of(entity));

        when(repository.searchHealthPackages(eq("test"), any())).thenReturn(page);
        when(mapper.toShortResponseDTO(entity))
                .thenReturn(HealthPackageShortResponseDTO.builder().id(id).build());

        PageResponse<HealthPackageShortResponseDTO> result =
                service.searchHealthPackages("test",0,10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldUpdateHealthPackage_whenValid() {
        UpdateHealthPackageRequestDTO request = UpdateHealthPackageRequestDTO.builder()
                .advanceBookingDays(20)
                .build();

        when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        HealthPackageResponseDTO result = service.updateHealthPackage(id, request);

        assertThat(result).isEqualTo(response);

        verify(mapper).updateEntity(request, entity);
    }

    @Test
    void shouldCancelAppointments_whenAdvanceBookingReduced() {
        UpdateHealthPackageRequestDTO request = UpdateHealthPackageRequestDTO.builder()
                .advanceBookingDays(10)
                .build();

        when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        service.updateHealthPackage(id, request);

        verify(appointmentRepository).cancelAppointmentsBeyondAdvanceBookingLimit(
                eq(id),
                any(LocalDate.class),
                eq(AppointmentStatus.CANCELLED),
                anySet()
        );
    }

    @Test
    void shouldDeleteHealthPackage_whenExists() {
        when(repository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(weeklyRepository.findAllByHealthPackageId(id)).thenReturn(List.of());
        when(slotRepository.findAllByHealthPackageId(id)).thenReturn(List.of());

        service.deleteHealthPackage(id);

        verify(repository).delete(entity);
        verify(weeklyRepository).deleteAll(any());
        verify(slotRepository).deleteAll(any());
    }

    @Test
    void shouldRestoreHealthPackage_whenDeleted() {
        entity.setIsDeleted(true);

        when(repository.findByIdIncludingDeleted(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        HealthPackageResponseDTO result = service.restoreHealthPackage(id);

        assertThat(result).isEqualTo(response);
        assertThat(entity.getIsDeleted()).isFalse();
    }

    @Test
    void shouldThrowConflict_whenRestoreAlreadyActive() {
        entity.setIsDeleted(false);

        when(repository.findByIdIncludingDeleted(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.restoreHealthPackage(id))
                .isInstanceOf(ConflictException.class);
    }
}