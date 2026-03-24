package com.healthcare.hospitalmanagementapi.user.mapper;

import com.healthcare.hospitalmanagementapi.department.mapper.DepartmentMapper;
import com.healthcare.hospitalmanagementapi.user.dto.group.CreateUserGroupRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.group.UpdateUserGroupRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.group.UserGroupResponseDTO;
import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = DepartmentMapper.class)
public interface UserGroupMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departments", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    UserGroup toEntity(CreateUserGroupRequestDTO dto);

    UserGroupResponseDTO toResponseDTO(UserGroup userGroup);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departments", ignore = true)
    void updateUserGroupFromDTO(UpdateUserGroupRequestDTO dto, @MappingTarget UserGroup userGroup);
}