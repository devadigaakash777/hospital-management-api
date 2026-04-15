package com.healthcare.hospitalmanagementapi.unit.timeslot;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.CreateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.DoctorTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.UpdateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import com.healthcare.hospitalmanagementapi.doctor.mapper.DoctorTimeSlotMapper;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.doctor.service.impl.DoctorTimeSlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorTimeSlotServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorTimeSlotRepository timeSlotRepository;

    @Mock
    private DoctorTimeSlotMapper timeSlotMapper;

    @InjectMocks
    private DoctorTimeSlotServiceImpl doctorTimeSlotService;

    private UUID doctorId;
    private UUID slotId;

    private Doctor doctor;
    private DoctorTimeSlot timeSlot;
    private CreateDoctorTimeSlotRequestDTO createRequest;
    private UpdateDoctorTimeSlotRequestDTO updateRequest;
    private DoctorTimeSlotResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        slotId = UUID.randomUUID();

        doctor = new Doctor();
        doctor.setId(doctorId);

        timeSlot = DoctorTimeSlot.builder()
                .id(slotId)
                .doctor(doctor)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .totalSlots(10)
                .reservedSlots(3)
                .patientsPerSlot(7)
                .build();

        createRequest = CreateDoctorTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .totalSlots(10)
                .reservedSlots(3)
                .build();

        updateRequest = UpdateDoctorTimeSlotRequestDTO.builder()
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .totalSlots(12)
                .reservedSlots(4)
                .build();

        responseDTO = DoctorTimeSlotResponseDTO.builder()
                .id(slotId)
                .doctorId(doctorId)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .totalSlots(10)
                .reservedSlots(3)
                .patientsPerSlot(7)
                .build();
    }

    @Test
    void shouldCreateTimeSlot_whenValidInput() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(timeSlotMapper.toEntity(createRequest)).thenReturn(timeSlot);
        when(timeSlotRepository.existsByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(
                doctorId,
                timeSlot.getEndTime(),
                timeSlot.getStartTime()
        )).thenReturn(false);
        when(timeSlotRepository.save(timeSlot)).thenReturn(timeSlot);
        when(timeSlotMapper.toResponseDTO(timeSlot)).thenReturn(responseDTO);

        DoctorTimeSlotResponseDTO result = doctorTimeSlotService.create(doctorId, createRequest);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(timeSlot.getDoctor()).isEqualTo(doctor);
        assertThat(timeSlot.getPatientsPerSlot()).isEqualTo(7);

        verify(doctorRepository).findById(doctorId);
        verify(timeSlotRepository).save(timeSlot);
        verify(timeSlotMapper).toResponseDTO(timeSlot);
    }

    @Test
    void shouldThrowException_whenCreateTimeSlotAndDoctorNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorTimeSlotService.create(doctorId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with id: " + doctorId);

        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictException_whenCreateTimeSlotAndStartTimeAfterEndTime() {
        timeSlot.setStartTime(LocalTime.of(10, 0));
        timeSlot.setEndTime(LocalTime.of(9, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(timeSlotMapper.toEntity(createRequest)).thenReturn(timeSlot);

        assertThatThrownBy(() -> doctorTimeSlotService.create(doctorId, createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Start time must be before end time");

        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictException_whenReservedSlotsGreaterThanTotalSlots() {
        timeSlot.setReservedSlots(15);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(timeSlotMapper.toEntity(createRequest)).thenReturn(timeSlot);

        assertThatThrownBy(() -> doctorTimeSlotService.create(doctorId, createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Reserved slots cannot be greater than total slots");

        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictException_whenCreateTimeSlotOverlapsExistingSlot() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(timeSlotMapper.toEntity(createRequest)).thenReturn(timeSlot);
        when(timeSlotRepository.existsByDoctorIdAndStartTimeLessThanAndEndTimeGreaterThan(
                doctorId,
                timeSlot.getEndTime(),
                timeSlot.getStartTime()
        )).thenReturn(true);

        assertThatThrownBy(() -> doctorTimeSlotService.create(doctorId, createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Time slot overlaps with an existing time slot");

        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTimeSlot_whenValidRequest() {
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(timeSlot));
        when(timeSlotRepository.existsByDoctorIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                doctorId,
                slotId,
                updateRequest.getEndTime(),
                updateRequest.getStartTime()
        )).thenReturn(false);
        when(timeSlotRepository.save(timeSlot)).thenReturn(timeSlot);
        when(timeSlotMapper.toResponseDTO(timeSlot)).thenReturn(responseDTO);

        doAnswer(invocation -> {
            UpdateDoctorTimeSlotRequestDTO dto = invocation.getArgument(0);
            DoctorTimeSlot slot = invocation.getArgument(1);

            slot.setStartTime(dto.getStartTime());
            slot.setEndTime(dto.getEndTime());
            slot.setTotalSlots(dto.getTotalSlots());
            slot.setReservedSlots(dto.getReservedSlots());

            return null;
        }).when(timeSlotMapper).updateEntity(updateRequest, timeSlot);

        DoctorTimeSlotResponseDTO result = doctorTimeSlotService.update(doctorId, slotId, updateRequest);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(timeSlot.getPatientsPerSlot()).isEqualTo(8);

        ArgumentCaptor<DoctorTimeSlot> captor = ArgumentCaptor.forClass(DoctorTimeSlot.class);
        verify(timeSlotRepository).save(captor.capture());

        assertThat(captor.getValue().getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(captor.getValue().getEndTime()).isEqualTo(LocalTime.of(10, 30));
    }

    @Test
    void shouldThrowException_whenUpdateTimeSlotAndSlotNotFound() {
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorTimeSlotService.update(doctorId, slotId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Time slot not found with id: " + slotId);
    }

    @Test
    void shouldThrowConflictException_whenUpdateTimeSlotOverlapsExistingSlot() {
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(timeSlot));

        doAnswer(invocation -> {
            DoctorTimeSlot slot = invocation.getArgument(1);
            slot.setStartTime(LocalTime.of(10, 0));
            slot.setEndTime(LocalTime.of(10, 30));
            return null;
        }).when(timeSlotMapper).updateEntity(updateRequest, timeSlot);

        when(timeSlotRepository.existsByDoctorIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                doctorId,
                slotId,
                LocalTime.of(10, 30),
                LocalTime.of(10, 0)
        )).thenReturn(true);

        assertThatThrownBy(() -> doctorTimeSlotService.update(doctorId, slotId, updateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Time slot overlaps with an existing time slot");

        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTimeSlot_whenSlotExistsForDoctor() {
        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(timeSlot));

        doctorTimeSlotService.delete(doctorId, slotId);

        verify(timeSlotRepository).delete(timeSlot);
    }

    @Test
    void shouldThrowException_whenDeleteTimeSlotAndSlotBelongsToAnotherDoctor() {
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(UUID.randomUUID());
        timeSlot.setDoctor(anotherDoctor);

        when(timeSlotRepository.findById(slotId)).thenReturn(Optional.of(timeSlot));

        assertThatThrownBy(() -> doctorTimeSlotService.delete(doctorId, slotId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Time slot not found with id: " + slotId);

        verify(timeSlotRepository, never()).delete(any());
    }

    @Test
    void shouldReturnAllTimeSlots_whenDoctorExists() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(timeSlotRepository.findAllByDoctorId(doctorId)).thenReturn(List.of(timeSlot));
        when(timeSlotMapper.toResponseDTO(timeSlot)).thenReturn(responseDTO);

        List<DoctorTimeSlotResponseDTO> result = doctorTimeSlotService.getAllByDoctor(doctorId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDTO);

        verify(timeSlotRepository).findAllByDoctorId(doctorId);
    }

    @Test
    void shouldThrowException_whenGetAllTimeSlotsAndDoctorNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorTimeSlotService.getAllByDoctor(doctorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with id: " + doctorId);

        verify(timeSlotRepository, never()).findAllByDoctorId(any());
    }
}