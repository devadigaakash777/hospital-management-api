package com.healthcare.hospitalmanagementapi.doctor.mapper;

import com.healthcare.hospitalmanagementapi.department.dto.DepartmentResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.doctor.*;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import org.mapstruct.*;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Doctor toEntity(CreateDoctorRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateDoctorRequestDTO dto, @MappingTarget Doctor doctor);

    @Mapping(target = "user", expression = "java(toUserSummaryDTO(doctor))")
    @Mapping(target = "department", expression = "java(toDepartmentResponseDTO(doctor))")
    DoctorResponseDTO toResponseDTO(Doctor doctor);

    default UserSummaryDTO toUserSummaryDTO(Doctor doctor) {
        if (doctor.getUser() == null) return null;
        return UserSummaryDTO.builder()
                .firstName(doctor.getUser().getFirstName())
                .lastName(doctor.getUser().getLastName())
                .email(doctor.getUser().getEmail())
                .build();
    }

    default DepartmentResponseDTO toDepartmentResponseDTO(Doctor doctor) {
        if (doctor.getDepartment() == null) return null;
        return DepartmentResponseDTO.builder()
                .id(doctor.getDepartment().getId())
                .departmentName(doctor.getDepartment().getDepartmentName())
                .build();
    }

    @Mapping(target = "fullName", expression = "java(getFullName(doctor))")
    DoctorShortResponseDTO toShortResponseDTO(Doctor doctor);

    default String getFullName(Doctor doctor) {
        if (doctor == null || doctor.getUser() == null) {
            return null;
        }

        String firstName = doctor.getUser().getFirstName();
        String lastName = doctor.getUser().getLastName();

        return ((firstName != null ? firstName : "") + " " +
                (lastName != null ? lastName : "")).trim();
    }
}