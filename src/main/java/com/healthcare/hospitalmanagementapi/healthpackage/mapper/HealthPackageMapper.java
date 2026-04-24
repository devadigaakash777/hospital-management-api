package com.healthcare.hospitalmanagementapi.healthpackage.mapper;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.CreateHealthPackageRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageAvailabilityResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.HealthPackageShortResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.healthpackage.UpdateHealthPackageRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.HealthPackageWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(
        componentModel = "spring",
        uses = {
                HealthPackageWeeklyScheduleMapper.class
        }
)
public interface HealthPackageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(
            target = "isActive",
            expression = "java(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE)"
    )
    HealthPackage toEntity(CreateHealthPackageRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateHealthPackageRequestDTO dto, @MappingTarget HealthPackage healthPackage);

    HealthPackageResponseDTO toResponseDTO(HealthPackage healthPackage);

    HealthPackageShortResponseDTO toShortResponseDTO(HealthPackage healthPackage);

    @Mapping(target = "id", source = "healthPackage.id")
    @Mapping(target = "packageName", source = "healthPackage.packageName")
    @Mapping(target = "advanceBookingDays", source = "healthPackage.advanceBookingDays")
    @Mapping(target = "weeklySchedules", source = "weeklySchedules")
    HealthPackageAvailabilityResponseDTO toAvailabilityResponseDTO(
            HealthPackage healthPackage,
            List<HealthPackageWeeklyScheduleResponseDTO> weeklySchedules
    );
}