package com.healthcare.hospitalmanagementapi.patient.mapper;

import com.healthcare.hospitalmanagementapi.patient.dto.CreatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.PatientResponseDTO;
import com.healthcare.hospitalmanagementapi.patient.dto.UpdatePatientRequestDTO;
import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uhId", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Patient toEntity(CreatePatientRequestDTO dto);

    PatientResponseDTO toResponseDTO(Patient patient);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uhId", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdatePatientRequestDTO dto, @MappingTarget Patient patient);
}