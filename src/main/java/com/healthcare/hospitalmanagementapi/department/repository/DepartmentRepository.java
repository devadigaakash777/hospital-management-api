package com.healthcare.hospitalmanagementapi.department.repository;

import com.healthcare.hospitalmanagementapi.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByIdAndIsDeletedFalse(UUID id);

    Page<Department> findAllByIsDeletedFalse(Pageable pageable);

    List<Department> findAllByIdInAndIsDeletedFalse(Set<UUID> ids);

    boolean existsByDepartmentNameAndIsDeletedFalse(String departmentName);

    @Query("""
        SELECT d
        FROM Department d
        WHERE d.isDeleted = false
        AND LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Department> searchDepartments(@Param("keyword") String keyword, Pageable pageable);
}