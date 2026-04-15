package com.healthcare.hospitalmanagementapi.doctor.mapper;

import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.CreateDoctorBlockedDateRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockeddate.DoctorBlockedDateResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorBlockedDate;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DoctorBlockedDateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    DoctorBlockedDate toEntity(CreateDoctorBlockedDateRequestDTO dto);

    @Mapping(target = "doctorId", source = "doctor.id")
    DoctorBlockedDateResponseDTO toResponseDTO(DoctorBlockedDate blockedDate);
}
