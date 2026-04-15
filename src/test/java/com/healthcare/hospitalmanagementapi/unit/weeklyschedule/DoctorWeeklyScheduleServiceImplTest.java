package com.healthcare.hospitalmanagementapi.unit.weeklyschedule;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.CreateDoctorWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.DoctorWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorWeeklySchedule;
import com.healthcare.hospitalmanagementapi.doctor.mapper.DoctorWeeklyScheduleMapper;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorWeeklyScheduleRepository;
import com.healthcare.hospitalmanagementapi.doctor.service.impl.DoctorWeeklyScheduleServiceImpl;
import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorWeeklyScheduleServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorWeeklyScheduleRepository weeklyScheduleRepository;

    @Mock
    private DoctorWeeklyScheduleMapper weeklyScheduleMapper;

    @InjectMocks
    private DoctorWeeklyScheduleServiceImpl doctorWeeklyScheduleService;

    private UUID doctorId;
    private UUID scheduleId;

    private Doctor doctor;
    private DoctorWeeklySchedule schedule;

    private CreateDoctorWeeklyScheduleRequestDTO mondayRequest;
    private CreateDoctorWeeklyScheduleRequestDTO tuesdayRequest;

    private DoctorWeeklyScheduleResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        doctor = new Doctor();
        doctor.setId(doctorId);

        mondayRequest = CreateDoctorWeeklyScheduleRequestDTO.builder()
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .build();

        tuesdayRequest = CreateDoctorWeeklyScheduleRequestDTO.builder()
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .build();

        schedule = DoctorWeeklySchedule.builder()
                .id(scheduleId)
                .doctor(doctor)
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .build();

        responseDTO = DoctorWeeklyScheduleResponseDTO.builder()
                .id(scheduleId)
                .doctorId(doctorId)
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .build();
    }

    @Test
    void shouldCreateWeeklySchedules_whenValidRequest() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        DoctorWeeklySchedule mondaySchedule = new DoctorWeeklySchedule();
        DoctorWeeklySchedule tuesdaySchedule = new DoctorWeeklySchedule();

        when(weeklyScheduleRepository.existsByDoctorAndWeekNumberAndDayOfWeek(
                doctor, 1, DayOfWeek.MONDAY)).thenReturn(false);

        when(weeklyScheduleRepository.existsByDoctorAndWeekNumberAndDayOfWeek(
                doctor, 1, DayOfWeek.TUESDAY)).thenReturn(false);

        when(weeklyScheduleMapper.toEntity(mondayRequest)).thenReturn(mondaySchedule);
        when(weeklyScheduleMapper.toEntity(tuesdayRequest)).thenReturn(tuesdaySchedule);

        when(weeklyScheduleRepository.saveAll(anyList()))
                .thenReturn(List.of(mondaySchedule, tuesdaySchedule));

        DoctorWeeklyScheduleResponseDTO mondayResponse = DoctorWeeklyScheduleResponseDTO.builder()
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .build();

        DoctorWeeklyScheduleResponseDTO tuesdayResponse = DoctorWeeklyScheduleResponseDTO.builder()
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .build();

        when(weeklyScheduleMapper.toResponseDTO(mondaySchedule)).thenReturn(mondayResponse);
        when(weeklyScheduleMapper.toResponseDTO(tuesdaySchedule)).thenReturn(tuesdayResponse);

        List<DoctorWeeklyScheduleResponseDTO> result = doctorWeeklyScheduleService.bulkCreate(
                doctorId,
                List.of(mondayRequest, tuesdayRequest)
        );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(DoctorWeeklyScheduleResponseDTO::getDayOfWeek)
                .containsExactly(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);

        ArgumentCaptor<List<DoctorWeeklySchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(weeklyScheduleRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .allMatch(savedSchedule -> savedSchedule.getDoctor().equals(doctor));
    }

    @Test
    void shouldThrowException_whenDoctorNotFoundDuringCreate() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorWeeklyScheduleService.bulkCreate(
                doctorId,
                List.of(mondayRequest)
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with id: " + doctorId);

        verify(weeklyScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowConflictException_whenDuplicateScheduleExistsInRequest() {
        CreateDoctorWeeklyScheduleRequestDTO duplicateRequest = CreateDoctorWeeklyScheduleRequestDTO.builder()
                .weekNumber(1)
                .dayOfWeek(DayOfWeek.MONDAY)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorWeeklyScheduleService.bulkCreate(
                doctorId,
                List.of(mondayRequest, duplicateRequest)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Duplicate schedule found in request for weekNumber 1 and day MONDAY"
                );

        verify(weeklyScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowConflictException_whenScheduleAlreadyExistsInDatabase() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        when(weeklyScheduleRepository.existsByDoctorAndWeekNumberAndDayOfWeek(
                doctor,
                1,
                DayOfWeek.MONDAY
        )).thenReturn(true);

        assertThatThrownBy(() -> doctorWeeklyScheduleService.bulkCreate(
                doctorId,
                List.of(mondayRequest)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Schedule already exists for weekNumber 1 and day MONDAY");

        verify(weeklyScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldUpdateWeeklySchedules_whenValidRequest() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        DoctorWeeklySchedule mondaySchedule = new DoctorWeeklySchedule();
        when(weeklyScheduleMapper.toEntity(mondayRequest)).thenReturn(mondaySchedule);

        when(weeklyScheduleRepository.saveAll(anyList()))
                .thenReturn(List.of(mondaySchedule));

        when(weeklyScheduleMapper.toResponseDTO(mondaySchedule)).thenReturn(responseDTO);

        List<DoctorWeeklyScheduleResponseDTO> result = doctorWeeklyScheduleService.bulkUpdate(
                doctorId,
                List.of(mondayRequest)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDTO);

        verify(weeklyScheduleRepository).deleteAllByDoctorId(doctorId);
        verify(weeklyScheduleRepository).flush();
        verify(weeklyScheduleRepository).saveAll(anyList());
    }

    @Test
    void shouldThrowException_whenDoctorNotFoundDuringUpdate() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorWeeklyScheduleService.bulkUpdate(
                doctorId,
                List.of(mondayRequest)
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with id: " + doctorId);

        verify(weeklyScheduleRepository, never()).deleteAllByDoctorId(any());
    }

    @Test
    void shouldThrowConflictException_whenDuplicateScheduleExistsInUpdateRequest() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorWeeklyScheduleService.bulkUpdate(
                doctorId,
                List.of(mondayRequest, mondayRequest)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Duplicate schedule found for weekNumber 1 and day MONDAY");

        verify(weeklyScheduleRepository, never()).deleteAllByDoctorId(any());
    }

    @Test
    void shouldDeleteWeeklySchedules_whenScheduleIdsAreValid() {
        when(weeklyScheduleRepository.findByIdAndDoctorId(scheduleId, doctorId))
                .thenReturn(Optional.of(schedule));

        doctorWeeklyScheduleService.bulkDelete(doctorId, List.of(scheduleId));

        verify(weeklyScheduleRepository).deleteAll(List.of(schedule));
    }

    @Test
    void shouldThrowException_whenDeletingNonExistingSchedule() {
        when(weeklyScheduleRepository.findByIdAndDoctorId(scheduleId, doctorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorWeeklyScheduleService.bulkDelete(
                doctorId,
                List.of(scheduleId)
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule not found with id: " + scheduleId);

        verify(weeklyScheduleRepository, never()).deleteAll(anyList());
    }

    @Test
    void shouldReturnAllWeeklySchedules_whenDoctorExists() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(weeklyScheduleRepository.findAllByDoctorId(doctorId))
                .thenReturn(List.of(schedule));
        when(weeklyScheduleMapper.toResponseDTO(schedule)).thenReturn(responseDTO);

        List<DoctorWeeklyScheduleResponseDTO> result =
                doctorWeeklyScheduleService.getAllByDoctor(doctorId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDTO);
    }

    @Test
    void shouldThrowException_whenDoctorNotFoundForGetAll() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorWeeklyScheduleService.getAllByDoctor(doctorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with id: " + doctorId);
    }

    @Test
    void shouldReturnWeeklySchedule_whenScheduleExists() {
        when(weeklyScheduleRepository.findByIdAndDoctorId(scheduleId, doctorId))
                .thenReturn(Optional.of(schedule));
        when(weeklyScheduleMapper.toResponseDTO(schedule)).thenReturn(responseDTO);

        DoctorWeeklyScheduleResponseDTO result =
                doctorWeeklyScheduleService.getById(doctorId, scheduleId);

        assertThat(result).isEqualTo(responseDTO);

        verify(weeklyScheduleMapper).toResponseDTO(schedule);
    }

    @Test
    void shouldThrowException_whenScheduleNotFoundById() {
        when(weeklyScheduleRepository.findByIdAndDoctorId(scheduleId, doctorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorWeeklyScheduleService.getById(doctorId, scheduleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule not found with id: " + scheduleId);
    }
}