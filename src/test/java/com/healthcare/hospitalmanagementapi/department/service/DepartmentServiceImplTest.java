package com.healthcare.hospitalmanagementapi.department.service;

import com.healthcare.hospitalmanagementapi.common.exception.custom.ConflictException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.ResourceNotFoundException;
import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.department.dto.*;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.mapper.DepartmentMapper;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.department.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.springframework.data.domain.*;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository repository;

    @Mock
    private DepartmentMapper mapper;

    @InjectMocks
    private DepartmentServiceImpl service;

    private UUID departmentId;
    private Department department;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        department = Department.builder()
                .id(departmentId)
                .departmentName("Cardiology")
                .build();
    }

    @Test
    void shouldCreateDepartment_whenValidInput() {
        CreateDepartmentRequestDTO dto = CreateDepartmentRequestDTO.builder()
                .departmentName("Cardiology")
                .build();

        DepartmentResponseDTO response = new DepartmentResponseDTO(departmentId, "Cardiology");

        when(repository.existsByDepartmentNameAndIsDeletedFalse("Cardiology")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(department);
        when(repository.save(department)).thenReturn(department);
        when(mapper.toResponseDTO(department)).thenReturn(response);

        DepartmentResponseDTO result = service.createDepartment(dto);

        assertThat(result.getDepartmentName()).isEqualTo("Cardiology");

        verify(repository).save(department);
        verify(mapper).toResponseDTO(department);
    }

    @Test
    void shouldThrowConflictException_whenDepartmentNameAlreadyExists() {
        CreateDepartmentRequestDTO dto = new CreateDepartmentRequestDTO("Cardiology");

        when(repository.existsByDepartmentNameAndIsDeletedFalse("Cardiology")).thenReturn(true);

        assertThatThrownBy(() -> service.createDepartment(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Department already exists");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldGetDepartmentById_whenExists() {
        when(repository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.of(department));
        when(mapper.toResponseDTO(department)).thenReturn(new DepartmentResponseDTO(departmentId, "Cardiology"));

        DepartmentResponseDTO result = service.getDepartmentById(departmentId);

        assertThat(result.getId()).isEqualTo(departmentId);
    }

    @Test
    void shouldThrowNotFound_whenDepartmentNotExists() {
        when(repository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDepartmentById(departmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department not found");
    }

    @Test
    void shouldUpdateDepartment_whenValid() {
        UpdateDepartmentRequestDTO dto = new UpdateDepartmentRequestDTO("Neurology");

        when(repository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.of(department));
        when(repository.existsByDepartmentNameAndIsDeletedFalse("Neurology")).thenReturn(false);
        when(repository.save(department)).thenReturn(department);
        when(mapper.toResponseDTO(department)).thenReturn(new DepartmentResponseDTO(departmentId, "Neurology"));

        DepartmentResponseDTO result = service.updateDepartment(departmentId, dto);

        assertThat(result.getDepartmentName()).isEqualTo("Neurology");

        verify(mapper).updateDepartmentFromDTO(dto, department);
    }

    @Test
    void shouldThrowConflict_whenUpdatingToExistingName() {
        UpdateDepartmentRequestDTO dto = new UpdateDepartmentRequestDTO("Radiology");

        when(repository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.of(department));
        when(repository.existsByDepartmentNameAndIsDeletedFalse("Radiology")).thenReturn(true);

        assertThatThrownBy(() -> service.updateDepartment(departmentId, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldDeleteDepartment_whenExists() {
        when(repository.findByIdAndIsDeletedFalse(departmentId)).thenReturn(Optional.of(department));

        service.deleteDepartment(departmentId);

        verify(repository).delete(department);
    }

    @Test
    void shouldRestoreDepartment_whenExists() {
        department.setIsDeleted(true);

        when(repository.findById(departmentId)).thenReturn(Optional.of(department));
        when(repository.save(department)).thenReturn(department);

        DepartmentResponseDTO responseDTO = DepartmentResponseDTO.builder()
                .id(departmentId)
                .departmentName(department.getDepartmentName())
                .build();

        when(mapper.toResponseDTO(department)).thenReturn(responseDTO);

        DepartmentResponseDTO result = service.restoreDepartment(departmentId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(departmentId);
        assertThat(result.getDepartmentName()).isEqualTo(department.getDepartmentName());
        assertThat(department.getIsDeleted()).isFalse();

        verify(repository).save(department);
        verify(mapper).toResponseDTO(department);
    }

    @Test
    void shouldThrowConflict_whenDepartmentAlreadyActive() {
        department.setIsDeleted(false);

        when(repository.findById(departmentId)).thenReturn(Optional.of(department));

        assertThatThrownBy(() -> service.restoreDepartment(departmentId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Department is already active and cannot be restored");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnPaginatedDepartments() {
        Page<Department> page = new PageImpl<>(java.util.List.of(department));

        when(repository.findAllByIsDeletedFalse(any())).thenReturn(page);
        when(mapper.toResponseDTO(department)).thenReturn(new DepartmentResponseDTO(departmentId, "Cardiology"));

        PageResponse<DepartmentResponseDTO> response = service.getAllDepartments(0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPageNumber()).isZero();
    }
}