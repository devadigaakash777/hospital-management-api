package com.healthcare.hospitalmanagementapi.healthpackage.mapper;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.CreateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.HealthPackageTimeSlotResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.timeslot.UpdateHealthPackageTimeSlotRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageTimeSlot;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface HealthPackageTimeSlotMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthPackage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    HealthPackageTimeSlot toEntity(CreateHealthPackageTimeSlotRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthPackage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(
            UpdateHealthPackageTimeSlotRequestDTO dto,
            @MappingTarget HealthPackageTimeSlot slot
    );

    @Mapping(target = "healthPackageId", source = "healthPackage.id")
    HealthPackageTimeSlotResponseDTO toResponseDTO(HealthPackageTimeSlot slot);
}