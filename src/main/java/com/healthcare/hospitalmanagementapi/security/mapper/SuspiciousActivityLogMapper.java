package com.healthcare.hospitalmanagementapi.security.mapper;

import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogRequestDTO;
import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogResponseDTO;
import com.healthcare.hospitalmanagementapi.security.entity.SuspiciousActivityLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SuspiciousActivityLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    SuspiciousActivityLog toEntity(SuspiciousActivityLogRequestDTO request);

    SuspiciousActivityLogResponseDTO toResponseDTO(SuspiciousActivityLog log);
}