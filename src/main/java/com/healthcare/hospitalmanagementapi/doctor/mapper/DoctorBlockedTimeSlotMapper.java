package com.healthcare.hospitalmanagementapi.doctor.mapper;

import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.CreateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.DoctorBlockedTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.blockedtime.UpdateDoctorBlockedTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorBlockedTimeSlot;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DoctorBlockedTimeSlotMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "batchId", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "reservedSlots", constant = "0")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    DoctorBlockedTimeSlot toEntity(CreateDoctorBlockedTimeSlotRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "batchId", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "reservedSlots", ignore = true)
    @Mapping(target = "blockedDate", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(
            UpdateDoctorBlockedTimeSlotRequestDTO dto,
            @MappingTarget DoctorBlockedTimeSlot entity
    );

    @Mapping(target = "doctorId", source = "doctor.id")
    DoctorBlockedTimeSlotResponseDTO toResponseDTO(DoctorBlockedTimeSlot blockedTimeSlot);
}

