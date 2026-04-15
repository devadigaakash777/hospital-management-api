package com.healthcare.hospitalmanagementapi.unit.doctor;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.CreateDoctorRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.DoctorResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.DoctorShortResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.UpdateDoctorRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.mapper.DoctorMapper;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorTimeSlotRepository;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorWeeklyScheduleRepository;
import com.healthcare.hospitalmanagementapi.doctor.service.impl.DoctorServiceImpl;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private DoctorWeeklyScheduleRepository weeklyScheduleRepository;

    @Mock
    private DoctorTimeSlotRepository timeSlotRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private UUID doctorId;
    private UUID userId;
    private UUID departmentId;

    private User user;
    private Department department;
    private Doctor doctor;
    private CreateDoctorRequestDTO createRequest;
    private UpdateDoctorRequestDTO updateRequest;
    private DoctorResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        userId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.STAFF);

        department = new Department();
        department.setId(departmentId);
        department.setDepartmentName("Cardiology");

        doctor = Doctor.builder()
                .id(doctorId)
                .user(user)
                .department(department)
                .qualification("MBBS")
                .designation("Consultant")
                .specialization("Cardiology")
                .advanceBookingDays(30)
                .isDeleted(false)
                .build();

        createRequest = CreateDoctorRequestDTO.builder()
                .userId(userId)
                .departmentId(departmentId)
                .qualification("MBBS")
                .designation("Consultant")
                .specialization("Cardiology")
                .advanceBookingDays(30)
                .build();

        updateRequest = UpdateDoctorRequestDTO.builder()
                .qualification("MBBS, MD")
                .departmentId(departmentId)
                .build();

        responseDTO = DoctorResponseDTO.builder()
                .id(doctorId)
                .qualification("MBBS")
                .designation("Consultant")
                .specialization("Cardiology")
                .build();
    }

    @Test
    void shouldCreateDoctor_whenValidInput() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(departmentRepository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.of(department));
        when(doctorMapper.toEntity(createRequest)).thenReturn(doctor);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(responseDTO);

        DoctorResponseDTO result = doctorService.createDoctor(createRequest);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(user.getRole()).isEqualTo(Role.DOCTOR);

        verify(userRepository).save(user);

        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(doctorCaptor.capture());

        Doctor savedDoctor = doctorCaptor.getValue();
        assertThat(savedDoctor.getUser()).isEqualTo(user);
        assertThat(savedDoctor.getDepartment()).isEqualTo(department);

        verify(doctorMapper).toResponseDTO(doctor);
    }

    @Test
    void shouldThrowException_whenCreateDoctorAndUserNotFound() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.createDoctor(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenCreateDoctorAndDepartmentNotFound() {
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(departmentRepository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.createDoctor(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department not found");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldReturnDoctor_whenDoctorExists() {
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(responseDTO);

        DoctorResponseDTO result = doctorService.getDoctorById(doctorId);

        assertThat(result).isEqualTo(responseDTO);

        verify(doctorRepository).findByIdAndIsDeletedFalse(doctorId);
        verify(doctorMapper).toResponseDTO(doctor);
    }

    @Test
    void shouldThrowException_whenDoctorNotFoundById() {
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(doctorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found");
    }

    @Test
    void shouldReturnPagedDoctors_whenGetAllDoctors() {
        Page<Doctor> doctorPage = new PageImpl<>(
                List.of(doctor),
                PageRequest.of(0, 10, Sort.by("createdAt").descending()),
                1
        );

        when(doctorRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(doctorPage);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(responseDTO);

        PageResponse<DoctorResponseDTO> result = doctorService.getAllDoctors(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void shouldReturnDoctorsByDepartment_whenDepartmentExists() {
        DoctorShortResponseDTO shortResponse = DoctorShortResponseDTO.builder()
                .id(doctorId)
                .fullName("John Doe")
                .build();

        when(departmentRepository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.of(department));
        when(doctorRepository.findByDepartmentIdAndIsDeletedFalseOrderByCreatedAtDesc(departmentId))
                .thenReturn(List.of(doctor));
        when(doctorMapper.toShortResponseDTO(doctor)).thenReturn(shortResponse);

        List<DoctorShortResponseDTO> result = doctorService.getDoctorsByDepartment(departmentId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
    }

    @Test
    void shouldThrowException_whenDepartmentNotFoundForGetDoctorsByDepartment() {
        when(departmentRepository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorsByDepartment(departmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department not found");

        verify(doctorRepository, never()).findByDepartmentIdAndIsDeletedFalseOrderByCreatedAtDesc(any());
    }

    @Test
    void shouldSearchDoctors_whenKeywordProvided() {
        Page<Doctor> doctorPage = new PageImpl<>(List.of(doctor));

        when(doctorRepository.searchDoctors(eq("john"), any(Pageable.class))).thenReturn(doctorPage);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(responseDTO);

        PageResponse<DoctorResponseDTO> result = doctorService.searchDoctors("john", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(responseDTO);
    }

    @Test
    void shouldUpdateDoctor_whenValidRequest() {
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId)).thenReturn(Optional.of(doctor));
        when(departmentRepository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.of(department));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(responseDTO);

        DoctorResponseDTO result = doctorService.updateDoctor(doctorId, updateRequest);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(doctor.getDepartment()).isEqualTo(department);

        verify(doctorMapper).updateEntity(updateRequest, doctor);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void shouldThrowException_whenUpdateDoctorAndDoctorNotFound() {
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.updateDoctor(doctorId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found");
    }

    @Test
    void shouldThrowException_whenUpdateDoctorAndDepartmentNotFound() {
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId)).thenReturn(Optional.of(doctor));
        when(departmentRepository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.updateDoctor(doctorId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department not found");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void shouldDeleteDoctorAndRelatedData_whenDoctorExists() {
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId)).thenReturn(Optional.of(doctor));
        when(weeklyScheduleRepository.findAllByDoctorId(doctorId)).thenReturn(List.of());
        when(timeSlotRepository.findAllByDoctorId(doctorId)).thenReturn(List.of());

        doctorService.deleteDoctor(doctorId);

        verify(weeklyScheduleRepository).deleteAll(anyList());
        verify(timeSlotRepository).deleteAll(anyList());
        verify(doctorRepository).delete(doctor);
    }

    @Test
    void shouldThrowException_whenDeleteDoctorAndDoctorNotFound() {
        when(doctorRepository.findByIdAndIsDeletedFalse(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.deleteDoctor(doctorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found");
    }

    @Test
    void shouldRestoreDoctor_whenDoctorIsDeleted() {
        doctor.setIsDeleted(true);
        doctor.setDeletedAt(LocalDateTime.now());

        when(doctorRepository.findByIdIncludingDeleted(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponseDTO(doctor)).thenReturn(responseDTO);

        DoctorResponseDTO result = doctorService.restoreDoctor(doctorId);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(doctor.getIsDeleted()).isFalse();
        assertThat(doctor.getDeletedAt()).isNull();

        verify(doctorRepository).save(doctor);
    }

    @Test
    void shouldThrowConflictException_whenRestoreDoctorAndDoctorAlreadyActive() {
        doctor.setIsDeleted(false);

        when(doctorRepository.findByIdIncludingDeleted(doctorId)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorService.restoreDoctor(doctorId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Doctor is already active and cannot be restored");
    }

    @Test
    void shouldThrowException_whenRestoreDoctorAndDoctorNotFound() {
        when(doctorRepository.findByIdIncludingDeleted(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.restoreDoctor(doctorId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found");
    }
}