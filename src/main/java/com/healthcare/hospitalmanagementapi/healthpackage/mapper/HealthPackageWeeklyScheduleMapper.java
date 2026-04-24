package com.healthcare.hospitalmanagementapi.healthpackage.mapper;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.CreateHealthPackageWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.weeklyschedule.HealthPackageWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageWeeklySchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HealthPackageWeeklyScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthPackage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    HealthPackageWeeklySchedule toEntity(CreateHealthPackageWeeklyScheduleRequestDTO dto);

    @Mapping(target = "healthPackageId", source = "healthPackage.id")
    HealthPackageWeeklyScheduleResponseDTO toResponseDTO(HealthPackageWeeklySchedule schedule);
}