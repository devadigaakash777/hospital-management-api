package com.healthcare.hospitalmanagementapi.user.repository;

import com.healthcare.hospitalmanagementapi.user.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {

    Optional<UserGroup> findByIdAndIsDeletedFalse(UUID id);

    Page<UserGroup> findAllByIsDeletedFalse(Pageable pageable);

    boolean existsByGroupNameAndIsDeletedFalse(String groupName);
}