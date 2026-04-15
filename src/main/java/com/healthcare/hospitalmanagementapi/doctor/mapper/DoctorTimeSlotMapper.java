package com.healthcare.hospitalmanagementapi.doctor.mapper;

import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.CreateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.DoctorTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.timeslot.UpdateDoctorTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorTimeSlot;
import org.mapstruct.*;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface DoctorTimeSlotMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    DoctorTimeSlot toEntity(CreateDoctorTimeSlotRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(UpdateDoctorTimeSlotRequestDTO dto, @MappingTarget DoctorTimeSlot slot);

    @Mapping(target = "doctorId", source = "doctor.id")
    DoctorTimeSlotResponseDTO toResponseDTO(DoctorTimeSlot slot);
}
