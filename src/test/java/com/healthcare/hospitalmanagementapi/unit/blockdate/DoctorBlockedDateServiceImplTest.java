package com.healthcare.hospitalmanagementapi.unit.blockdate;

import com.healthcare.hospitalmanagementapi.appointment.repository.AppointmentRepository;
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
import com.healthcare.hospitalmanagementapi.doctor.service.impl.DoctorBlockedDateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorBlockedDateServiceImplTest {

    @Mock
    private DoctorBlockedDateRepository blockedDateRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorBlockedDateMapper blockedDateMapper;

    @Mock
    private DoctorTimeSlotRepository timeSlotRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DoctorBlockedDateServiceImpl service;

    private UUID doctorId;
    private UUID blockedDateId;

    private Doctor doctor;
    private DoctorBlockedDate blockedDate;
    private CreateDoctorBlockedDateRequestDTO createRequest;
    private DoctorBlockedDateResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        blockedDateId = UUID.randomUUID();

        doctor = new Doctor();
        doctor.setId(doctorId);

        blockedDate = DoctorBlockedDate.builder()
                .id(blockedDateId)
                .doctor(doctor)
                .blockedDate(LocalDate.now().plusDays(1))
                .blockReason("Conference")
                .build();

        createRequest = CreateDoctorBlockedDateRequestDTO.builder()
                .blockedDate(LocalDate.now().plusDays(1))
                .blockReason("Conference")
                .build();

        responseDTO = DoctorBlockedDateResponseDTO.builder()
                .id(blockedDateId)
                .doctorId(doctorId)
                .blockedDate(createRequest.getBlockedDate())
                .blockReason("Conference")
                .build();
    }

    @Test
    void shouldCreateBlockedDate_whenValidInput() {
        when(blockedDateRepository.existsByDoctor_IdAndBlockedDateAndIsDeletedFalse(
                doctorId,
                createRequest.getBlockedDate()
        )).thenReturn(false);

        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));

        when(blockedDateMapper.toEntity(createRequest))
                .thenReturn(blockedDate);

        when(blockedDateRepository.save(blockedDate))
                .thenReturn(blockedDate);

        when(blockedDateMapper.toResponseDTO(blockedDate))
                .thenReturn(responseDTO);

        DoctorBlockedDateResponseDTO result =
                service.createBlockedDate(doctorId, createRequest);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<DoctorBlockedDate> captor =
                ArgumentCaptor.forClass(DoctorBlockedDate.class);

        verify(blockedDateRepository).save(captor.capture());

        assertThat(captor.getValue().getDoctor()).isEqualTo(doctor);

        verify(blockedDateMapper).toResponseDTO(blockedDate);
    }

    @Test
    void shouldThrowConflictException_whenBlockedDateAlreadyExists() {
        when(blockedDateRepository.existsByDoctor_IdAndBlockedDateAndIsDeletedFalse(
                doctorId,
                createRequest.getBlockedDate()
        )).thenReturn(true);

        assertThatThrownBy(() -> service.createBlockedDate(doctorId, createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Doctor already has a blocked date on " + createRequest.getBlockedDate());

        verify(doctorRepository, never()).findByIdAndIsDeletedFalse(any());
        verify(blockedDateRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundException_whenDoctorDoesNotExist() {
        when(blockedDateRepository.existsByDoctor_IdAndBlockedDateAndIsDeletedFalse(
                doctorId,
                createRequest.getBlockedDate()
        )).thenReturn(false);

        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBlockedDate(doctorId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found");
    }

    @Test
    void shouldReturnBlockedDate_whenGetBlockedDateById() {
        when(blockedDateRepository.findByIdAndIsDeletedFalse(blockedDateId))
                .thenReturn(Optional.of(blockedDate));

        when(blockedDateMapper.toResponseDTO(blockedDate))
                .thenReturn(responseDTO);

        DoctorBlockedDateResponseDTO result =
                service.getBlockedDateById(blockedDateId);

        assertThat(result).isEqualTo(responseDTO);

        verify(blockedDateRepository).findByIdAndIsDeletedFalse(blockedDateId);
        verify(blockedDateMapper).toResponseDTO(blockedDate);
    }

    @Test
    void shouldThrowResourceNotFoundException_whenBlockedDateNotFound() {
        when(blockedDateRepository.findByIdAndIsDeletedFalse(blockedDateId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBlockedDateById(blockedDateId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Blocked date not found");
    }

    @Test
    void shouldReturnPagedBlockedDates_whenGetBlockedDatesByDoctor() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(blockedDateRepository.findAllByDoctor_IdAndIsDeletedFalse(doctorId, pageable))
                .thenReturn(new PageImpl<>(List.of(blockedDate), pageable, 1));

        when(blockedDateMapper.toResponseDTO(blockedDate))
                .thenReturn(responseDTO);

        PageResponse<DoctorBlockedDateResponseDTO> result =
                service.getBlockedDatesByDoctor(doctorId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(responseDTO);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void shouldReturnPagedBlockedDates_whenGetBlockedDatesByDateRange() {
        PageRequest pageable = PageRequest.of(0, 10);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        when(blockedDateRepository
                .findAllByDoctor_IdAndBlockedDateBetweenAndIsDeletedFalse(
                        doctorId,
                        startDate,
                        endDate,
                        pageable
                ))
                .thenReturn(new PageImpl<>(List.of(blockedDate), pageable, 1));

        when(blockedDateMapper.toResponseDTO(blockedDate))
                .thenReturn(responseDTO);

        PageResponse<DoctorBlockedDateResponseDTO> result =
                service.getBlockedDatesByDateRange(
                        doctorId,
                        startDate,
                        endDate,
                        pageable
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldThrowConflictException_whenStartDateIsAfterEndDate() {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now();

        assertThatThrownBy(() -> service.getBlockedDatesByDateRange(
                doctorId,
                startDate,
                endDate,
                PageRequest.of(0, 10)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start date cannot be after end date");

        verify(blockedDateRepository, never())
                .findAllByDoctor_IdAndBlockedDateBetweenAndIsDeletedFalse(
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldDeleteBlockedDate_whenFutureBlockedDateExists() {
        blockedDate.setBlockedDate(LocalDate.now().plusDays(1));

        when(blockedDateRepository.findByIdAndIsDeletedFalse(blockedDateId))
                .thenReturn(Optional.of(blockedDate));

        service.deleteBlockedDate(blockedDateId);

        verify(blockedDateRepository).delete(blockedDate);
        verify(timeSlotRepository, never()).findAllByDoctorId(any());
    }

    @Test
    void shouldDeleteBlockedDate_whenTodayBlockedDateAndBeforeFirstTimeSlot() {
        blockedDate.setBlockedDate(LocalDate.now());

        DoctorTimeSlot slot = new DoctorTimeSlot();
        slot.setStartTime(LocalTime.now().plusHours(1));

        when(blockedDateRepository.findByIdAndIsDeletedFalse(blockedDateId))
                .thenReturn(Optional.of(blockedDate));

        when(timeSlotRepository.findAllByDoctorId(doctorId))
                .thenReturn(List.of(slot));

        service.deleteBlockedDate(blockedDateId);

        verify(timeSlotRepository).findAllByDoctorId(doctorId);
        verify(blockedDateRepository).delete(blockedDate);
    }

    @Test
    void shouldThrowConflictException_whenDeletingTodayBlockedDateAfterFirstTimeSlotStarted() {
        blockedDate.setBlockedDate(LocalDate.now());

        DoctorTimeSlot slot = new DoctorTimeSlot();
        slot.setStartTime(LocalTime.now().minusMinutes(10));

        when(blockedDateRepository.findByIdAndIsDeletedFalse(blockedDateId))
                .thenReturn(Optional.of(blockedDate));

        when(timeSlotRepository.findAllByDoctorId(doctorId))
                .thenReturn(List.of(slot));

        assertThatThrownBy(() -> service.deleteBlockedDate(blockedDateId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Blocked date can only be deleted before the first doctor time slot starts");

        verify(blockedDateRepository, never()).delete(any());
    }

    @Test
    void shouldThrowResourceNotFoundException_whenDeletingNonExistingBlockedDate() {
        when(blockedDateRepository.findByIdAndIsDeletedFalse(blockedDateId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteBlockedDate(blockedDateId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Blocked date not found");
    }
}