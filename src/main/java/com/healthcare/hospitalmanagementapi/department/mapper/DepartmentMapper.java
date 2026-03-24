package com.healthcare.hospitalmanagementapi.department.mapper;

import com.healthcare.hospitalmanagementapi.department.dto.*;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userGroups", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    Department toEntity(CreateDepartmentRequestDTO dto);

    DepartmentResponseDTO toResponseDTO(Department department);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userGroups", ignore = true)
    @Mapping(target = "users", ignore = true)
    void updateDepartmentFromDTO(UpdateDepartmentRequestDTO dto, @MappingTarget Department department);
}
