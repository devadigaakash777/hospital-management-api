package com.healthcare.hospitalmanagementapi.unit.department.repository;

import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Transactional
class DepartmentRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department cardiology;
    private Department neurology;
    private Department deletedDepartment;

    @BeforeEach
    void setUp() {
        cardiology = createDepartment("Cardiology");
        neurology = createDepartment("Neurology");

        deletedDepartment = createDepartment("Radiology");
        deletedDepartment.setIsDeleted(true);
        deletedDepartment = departmentRepository.saveAndFlush(deletedDepartment);
    }

    @Test
    void shouldReturnDepartment_whenFindByIdAndIsDeletedFalse() {
        Optional<Department> result = departmentRepository.findByIdAndIsDeletedFalse(cardiology.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(cardiology.getId());
    }

    @Test
    void shouldNotReturnDeletedDepartment_whenFindByIdAndIsDeletedFalse() {
        Optional<Department> result = departmentRepository.findByIdAndIsDeletedFalse(deletedDepartment.getId());

        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnOnlyNonDeletedDepartments_whenFindAllByIsDeletedFalse() {
        Page<Department> result = departmentRepository.findAllByIsDeletedFalse(
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Department::getId)
                .contains(cardiology.getId(), neurology.getId())
                .doesNotContain(deletedDepartment.getId());
    }

    @Test
    void shouldReturnDepartmentsByIds_whenFindAllByIdInAndIsDeletedFalse() {
        Set<UUID> ids = Set.of(
                cardiology.getId(),
                deletedDepartment.getId()
        );

        List<Department> result = departmentRepository.findAllByIdInAndIsDeletedFalse(ids);

        assertThat(result)
                .extracting(Department::getId)
                .contains(cardiology.getId())
                .doesNotContain(deletedDepartment.getId());
    }

    @Test
    void shouldReturnTrue_whenExistsByDepartmentNameAndIsDeletedFalse() {
        boolean exists = departmentRepository.existsByDepartmentNameAndIsDeletedFalse("Cardiology");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenDeletedDepartmentExistsByDepartmentNameAndIsDeletedFalse() {
        boolean exists = departmentRepository.existsByDepartmentNameAndIsDeletedFalse("Radiology");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldSearchDepartmentsByName_whenKeywordMatchesDepartmentName() {
        Page<Department> result = departmentRepository.searchDepartments(
                "cardio",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(cardiology);
    }

    @Test
    void shouldSearchDepartmentsCaseInsensitive_whenKeywordMatchesDepartmentName() {
        Page<Department> result = departmentRepository.searchDepartments(
                "NEUROLOGY",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(neurology);
    }

    @Test
    void shouldNotReturnDeletedDepartments_whenSearchDepartments() {
        Page<Department> result = departmentRepository.searchDepartments(
                "radiology",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    private Department createDepartment(String departmentName) {
        Department department = new Department();
        department.setDepartmentName(departmentName);

        return departmentRepository.saveAndFlush(department);
    }
}