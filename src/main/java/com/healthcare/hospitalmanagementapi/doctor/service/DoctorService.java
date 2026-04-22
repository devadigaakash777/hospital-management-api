package com.healthcare.hospitalmanagementapi.doctor.service;

import com.healthcare.hospitalmanagementapi.common.response.PageResponse;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.*;

import java.util.List;
import java.util.UUID;

public interface DoctorService {

    DoctorResponseDTO createDoctor(CreateDoctorRequestDTO dto);

    DoctorResponseDTO getDoctorById(UUID id);

    PageResponse<DoctorResponseDTO> getAllDoctors(int page, int size);

    PageResponse<DoctorResponseDTO> getAllDoctorsIncDel(int page, int size);

    List<DoctorShortResponseDTO> getDoctorsByDepartment(UUID departmentId);

    PageResponse<DoctorResponseDTO> searchDoctors(String name, int page, int size);

    DoctorResponseDTO updateDoctor(UUID id, UpdateDoctorRequestDTO dto);

    void deleteDoctor(UUID id);

    DoctorAvailabilityResponseDTO getDoctorAvailability(UUID doctorId);

    DoctorResponseDTO restoreDoctor(UUID id);
}
