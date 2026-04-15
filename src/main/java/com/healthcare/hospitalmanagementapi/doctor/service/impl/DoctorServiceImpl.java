package com.healthcare.hospitalmanagementapi.doctor.service.impl;

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
import com.healthcare.hospitalmanagementapi.doctor.service.DoctorService;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@CacheConfig(cacheNames = "doctors")
public class DoctorServiceImpl implements DoctorService {

    private static final String DOCTOR_NOT_FOUND_MESSAGE = "Doctor not found";
    private static final String DEPARTMENT_NOT_FOUND_MESSAGE = "Department not found";
    private static final String CREATED_AT = "createdAt";

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorMapper doctorMapper;
    private final DoctorWeeklyScheduleRepository weeklyScheduleRepository;
    private final DoctorTimeSlotRepository timeSlotRepository;

    @Override
    @CachePut(key = "#result.id")
    public DoctorResponseDTO createDoctor(CreateDoctorRequestDTO dto) {

        User user = userRepository.findByIdAndIsDeletedFalse(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Department department = departmentRepository.findByIdAndIsDeletedFalse(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT_NOT_FOUND_MESSAGE));

        user.setRole(Role.DOCTOR);
        userRepository.save(user);

        Doctor doctor = doctorMapper.toEntity(dto);
        doctor.setUser(user);
        doctor.setDepartment(department);

        Doctor saved = doctorRepository.save(doctor);

        log.info("Doctor created with id: {}", saved.getId());

        return doctorMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public DoctorResponseDTO getDoctorById(UUID id) {

        Doctor doctor = doctorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MESSAGE));

        return doctorMapper.toResponseDTO(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponseDTO> getAllDoctorsIncDel(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "created_at")
        );

        Page<Doctor> doctorPage = doctorRepository.findAllIncludingDeleted(pageable);

        Page<DoctorResponseDTO> dtoPage = doctorPage.map(doctorMapper::toResponseDTO);

        return PageResponse.of(dtoPage);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponseDTO> getAllDoctors(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(CREATED_AT).descending());

        Page<Doctor> doctorPage = doctorRepository.findAllByIsDeletedFalse(pageable);

        Page<DoctorResponseDTO> dtoPage = doctorPage.map(doctorMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorShortResponseDTO> getDoctorsByDepartment(UUID departmentId) {

        departmentRepository.findByIdAndIsDeletedFalse(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT_NOT_FOUND_MESSAGE));

        List<Doctor> doctors = doctorRepository
                .findByDepartmentIdAndIsDeletedFalseOrderByCreatedAtDesc(departmentId);

        return doctors.stream()
                .map(doctorMapper::toShortResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponseDTO> searchDoctors(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(CREATED_AT).descending());

        Page<Doctor> doctorPage = doctorRepository.searchDoctors(keyword, pageable);

        Page<DoctorResponseDTO> dtoPage = doctorPage.map(doctorMapper::toResponseDTO);

        return new PageResponse<>(dtoPage);
    }

    @Override
    @CachePut(key = "#id")
    public DoctorResponseDTO updateDoctor(UUID id, UpdateDoctorRequestDTO dto) {

        Doctor doctor = doctorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MESSAGE));

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndIsDeletedFalse(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(DEPARTMENT_NOT_FOUND_MESSAGE));

            doctor.setDepartment(department);
        }

        doctorMapper.updateEntity(dto, doctor);

        Doctor updated = doctorRepository.save(doctor);

        log.info("Doctor updated with id: {}", id);

        return doctorMapper.toResponseDTO(updated);
    }

    @Override
    @CacheEvict(key = "#id")
    public void deleteDoctor(UUID id) {

        Doctor doctor = doctorRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MESSAGE));

        weeklyScheduleRepository.deleteAll(
                weeklyScheduleRepository.findAllByDoctorId(id)
        );

        timeSlotRepository.deleteAll(
                timeSlotRepository.findAllByDoctorId(id)
        );

        doctorRepository.delete(doctor);

        log.info("Doctor and all related weekly schedules and time slots deleted with id: {}", id);
    }

    @Override
    @CachePut(key = "#id")
    public DoctorResponseDTO restoreDoctor(UUID id) {

        Doctor doctor = doctorRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MESSAGE));

        if (!Boolean.TRUE.equals(doctor.getIsDeleted())) {
            throw new ConflictException("Doctor is already active and cannot be restored");
        }

        doctor.setIsDeleted(false);
        doctor.setDeletedAt(null);

        Doctor saved = doctorRepository.save(doctor);

        log.info("Doctor restored with id: {}", id);

        return doctorMapper.toResponseDTO(saved);
    }
}
