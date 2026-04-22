package com.healthcare.hospitalmanagementapi.unit.blockedtimeslot;

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
import com.healthcare.hospitalmanagementapi.doctor.service.impl.DoctorBlockedTimeSlotServiceImpl;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorBlockedTimeSlotServiceImplTest {

    @Mock
    private DoctorBlockedTimeSlotRepository blockedTimeSlotRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorBlockedTimeSlotMapper blockedTimeSlotMapper;

    @InjectMocks
    private DoctorBlockedTimeSlotServiceImpl service;

    private UUID doctorId;
    private UUID blockedTimeSlotId;

    private Doctor doctor;
    private DoctorBlockedTimeSlot blockedTimeSlot;
    private CreateDoctorBlockedTimeSlotRequestDTO createRequest;
    private UpdateDoctorBlockedTimeSlotRequestDTO updateRequest;
    private DoctorBlockedTimeSlotResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        blockedTimeSlotId = UUID.randomUUID();

        doctor = new Doctor();
        doctor.setId(doctorId);

        blockedTimeSlot = DoctorBlockedTimeSlot.builder()
                .id(blockedTimeSlotId)
                .doctor(doctor)
                .blockedDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .reservedSlots(2)
                .blockReason("Conference")
                .build();

        createRequest = CreateDoctorBlockedTimeSlotRequestDTO.builder()
                .blockedDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .blockReason("Conference")
                .build();

        updateRequest = UpdateDoctorBlockedTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(13, 0))
                .blockReason("Updated reason")
                .build();

        responseDTO = DoctorBlockedTimeSlotResponseDTO.builder()
                .id(blockedTimeSlotId)
                .doctorId(doctorId)
                .blockedDate(createRequest.getBlockedDate())
                .startTime(createRequest.getStartTime())
                .endTime(createRequest.getEndTime())
                .blockReason("Conference")
                .build();
    }

    @Test
    void shouldCreateBlockedTimeSlot_whenValidInput() {
        when(blockedTimeSlotRepository
                .existsByDoctor_IdAndBlockedDateAndStartTimeLessThanAndEndTimeGreaterThanAndIsDeletedFalse(
                        any(), any(), any(), any()))
                .thenReturn(false);

        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.of(doctor));

        when(blockedTimeSlotMapper.toEntity(createRequest))
                .thenReturn(blockedTimeSlot);

        when(blockedTimeSlotRepository.save(blockedTimeSlot))
                .thenReturn(blockedTimeSlot);

        when(blockedTimeSlotMapper.toResponseDTO(blockedTimeSlot))
                .thenReturn(responseDTO);

        DoctorBlockedTimeSlotResponseDTO result =
                service.createBlockedTimeSlot(doctorId, createRequest);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<DoctorBlockedTimeSlot> captor =
                ArgumentCaptor.forClass(DoctorBlockedTimeSlot.class);

        verify(blockedTimeSlotRepository).save(captor.capture());

        DoctorBlockedTimeSlot savedEntity = captor.getValue();
        assertThat(savedEntity.getDoctor()).isEqualTo(doctor);
        assertThat(savedEntity.getBatchId()).isNull();

        verify(blockedTimeSlotMapper).toResponseDTO(blockedTimeSlot);
    }

    @Test
    void shouldThrowConflictException_whenStartTimeIsAfterEndTime() {
        createRequest.setStartTime(LocalTime.of(15, 0));
        createRequest.setEndTime(LocalTime.of(14, 0));

        assertThatThrownBy(() -> service.createBlockedTimeSlot(doctorId, createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start time must be before end time");

        verify(blockedTimeSlotRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictException_whenOverlappingBlockedSlotExists() {
        when(blockedTimeSlotRepository
                .existsByDoctor_IdAndBlockedDateAndStartTimeLessThanAndEndTimeGreaterThanAndIsDeletedFalse(
                        any(), any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createBlockedTimeSlot(doctorId, createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Blocked time slot overlaps with an existing blocked time slot");

        verify(doctorRepository, never()).findByIdAndIsDeletedFalse(any());
    }

    @Test
    void shouldThrowResourceNotFoundException_whenDoctorDoesNotExist() {
        when(blockedTimeSlotRepository
                .existsByDoctor_IdAndBlockedDateAndStartTimeLessThanAndEndTimeGreaterThanAndIsDeletedFalse(
                        any(), any(), any(), any()))
                .thenReturn(false);

        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBlockedTimeSlot(doctorId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found");
    }

    @Test
    void shouldReturnBlockedTimeSlot_whenGetById() {
        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        when(blockedTimeSlotMapper.toResponseDTO(blockedTimeSlot))
                .thenReturn(responseDTO);

        DoctorBlockedTimeSlotResponseDTO result =
                service.getBlockedTimeSlotById(blockedTimeSlotId);

        assertThat(result).isEqualTo(responseDTO);
    }

    @Test
    void shouldThrowResourceNotFoundException_whenBlockedTimeSlotDoesNotExist() {
        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBlockedTimeSlotById(blockedTimeSlotId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Blocked time slot not found");
    }

    @Test
    void shouldReturnPagedBlockedTimeSlots_whenGetBlockedTimeSlotsByDoctor() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(blockedTimeSlotRepository.findAllByDoctor_IdAndIsDeletedFalse(
                doctorId,
                pageable))
                .thenReturn(new PageImpl<>(
                        List.of(blockedTimeSlot),
                        pageable,
                        1
                ));

        when(blockedTimeSlotMapper.toResponseDTO(blockedTimeSlot))
                .thenReturn(responseDTO);

        PageResponse<DoctorBlockedTimeSlotResponseDTO> result =
                service.getBlockedTimeSlotsByDoctor(doctorId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageNumber()).isZero();

        assertThat(result.getPageSize()).isEqualTo(10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void shouldUpdateBlockedTimeSlot_whenValidFutureSlot() {
        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        when(blockedTimeSlotRepository.save(blockedTimeSlot))
                .thenReturn(blockedTimeSlot);

        when(blockedTimeSlotMapper.toResponseDTO(blockedTimeSlot))
                .thenReturn(responseDTO);

        DoctorBlockedTimeSlotResponseDTO result =
                service.updateBlockedTimeSlot(blockedTimeSlotId, updateRequest);

        assertThat(result).isEqualTo(responseDTO);

        verify(blockedTimeSlotMapper)
                .updateEntityFromDto(updateRequest, blockedTimeSlot);

        verify(blockedTimeSlotRepository).save(blockedTimeSlot);
    }

    @Test
    void shouldThrowConflictException_whenUpdatingPastBlockedSlot() {
        blockedTimeSlot.setBlockedDate(LocalDate.now().minusDays(1));

        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        assertThatThrownBy(() -> service.updateBlockedTimeSlot(blockedTimeSlotId, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Past blocked time slot cannot be updated");
    }

    @Test
    void shouldThrowConflictException_whenUpdatingStartTimeAfterSlotStarted() {
        blockedTimeSlot.setBlockedDate(LocalDate.now());
        blockedTimeSlot.setStartTime(LocalTime.now().minusMinutes(10));

        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        updateRequest.setStartTime(LocalTime.now().plusMinutes(10));

        assertThatThrownBy(() -> service.updateBlockedTimeSlot(blockedTimeSlotId, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start time can only be updated before the blocked slot start time");
    }

    @Test
    void shouldThrowConflictException_whenEndTimeIsBeforeCurrentTime() {
        blockedTimeSlot.setBlockedDate(LocalDate.now());

        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        updateRequest.setStartTime(null);
        updateRequest.setEndTime(LocalTime.now().minusMinutes(5));

        assertThatThrownBy(() -> service.updateBlockedTimeSlot(blockedTimeSlotId, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("End time must be greater than current time");
    }

    @Test
    void shouldThrowConflictException_whenUpdatedEndTimeIsBeforeStartTime() {
        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        updateRequest.setStartTime(LocalTime.of(14, 0));
        updateRequest.setEndTime(LocalTime.of(13, 0));

        doAnswer(invocation -> {
            DoctorBlockedTimeSlot entity = invocation.getArgument(1);
            entity.setStartTime(updateRequest.getStartTime());
            entity.setEndTime(updateRequest.getEndTime());
            return null;
        }).when(blockedTimeSlotMapper).updateEntityFromDto(eq(updateRequest), any());

        assertThatThrownBy(() -> service.updateBlockedTimeSlot(blockedTimeSlotId, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("End time must be after start time");
    }

    @Test
    void shouldDeleteBlockedTimeSlot_whenFutureSlotExists() {
        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        service.deleteBlockedTimeSlot(blockedTimeSlotId);

        verify(blockedTimeSlotRepository).delete(blockedTimeSlot);
    }

    @Test
    void shouldThrowConflictException_whenDeletingPastBlockedTimeSlot() {
        blockedTimeSlot.setBlockedDate(LocalDate.now().minusDays(1));

        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        assertThatThrownBy(() -> service.deleteBlockedTimeSlot(blockedTimeSlotId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Past blocked time slot cannot be deleted");
    }

    @Test
    void shouldThrowConflictException_whenDeletingStartedBlockedTimeSlot() {
        blockedTimeSlot.setBlockedDate(LocalDate.now());
        blockedTimeSlot.setStartTime(LocalTime.now().minusMinutes(5));

        when(blockedTimeSlotRepository.findByIdAndIsDeletedFalse(blockedTimeSlotId))
                .thenReturn(Optional.of(blockedTimeSlot));

        assertThatThrownBy(() -> service.deleteBlockedTimeSlot(blockedTimeSlotId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Blocked time slot can only be deleted before the blocked start time");
    }
}