package com.healthcare.hospitalmanagementapi.user.mapper;

import com.healthcare.hospitalmanagementapi.department.dto.DepartmentResponseDTO;
import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.user.dto.group.UserGroupSummaryDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.CreateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.UpdateUserRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.user.UserResponseDTO;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "departments", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    User toEntity(CreateUserRequestDTO dto);

    @Mapping(target = "group", source = "group")
    @Mapping(target = "departments", source = "departments")
    UserResponseDTO toResponseDTO(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "departments", ignore = true)
    void updateUserFromDTO(UpdateUserRequestDTO dto, @MappingTarget User user);

    DepartmentResponseDTO map(Department department);

    Set<DepartmentResponseDTO> mapDepartments(Set<Department> departments);

    UserGroupSummaryDTO map(UserGroup group);
}