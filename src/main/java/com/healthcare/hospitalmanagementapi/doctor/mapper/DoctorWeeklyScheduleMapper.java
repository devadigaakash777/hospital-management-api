package com.healthcare.hospitalmanagementapi.doctor.mapper;

import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.CreateDoctorWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.DoctorWeeklyScheduleResponseDTO;
import com.healthcare.hospitalmanagementapi.doctor.entity.DoctorWeeklySchedule;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface DoctorWeeklyScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    DoctorWeeklySchedule toEntity(CreateDoctorWeeklyScheduleRequestDTO dto);

    @Mapping(target = "doctorId", source = "doctor.id")
    DoctorWeeklyScheduleResponseDTO toResponseDTO(DoctorWeeklySchedule schedule);
}
