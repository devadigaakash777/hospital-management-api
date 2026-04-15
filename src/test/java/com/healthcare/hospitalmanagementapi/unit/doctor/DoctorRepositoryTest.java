package com.healthcare.hospitalmanagementapi.unit.doctor;

import com.healthcare.hospitalmanagementapi.department.entity.Department;
import com.healthcare.hospitalmanagementapi.department.repository.DepartmentRepository;
import com.healthcare.hospitalmanagementapi.doctor.entity.Doctor;
import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Transactional
class DoctorRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department cardiology;
    private Department neurology;

    @BeforeEach
    void setUp() {
        cardiology = new Department();
        cardiology.setDepartmentName("Cardiology");
        cardiology = departmentRepository.save(cardiology);

        neurology = new Department();
        neurology.setDepartmentName("Neurology");
        neurology = departmentRepository.save(neurology);
    }

    @Test
    void shouldReturnDoctorIncludingDeleted_whenFindByIdIncludingDeleted() {
        Doctor doctor = createDoctor("John", "Doe", cardiology);
        doctor.setIsDeleted(true);
        doctorRepository.saveAndFlush(doctor);

        Optional<Doctor> result = doctorRepository.findByIdIncludingDeleted(doctor.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(doctor.getId());
        assertThat(result.get().getIsDeleted()).isTrue();
    }

    @Test
    void shouldSearchDoctorsByFirstName_whenKeywordMatchesFirstName() {
        Doctor doctor = createDoctor("John", "Doe", cardiology);

        Page<Doctor> result = doctorRepository.searchDoctors(
                "john",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(Doctor::getId)
                .contains(doctor.getId());
    }

    @Test
    void shouldSearchDoctorsByFullName_whenKeywordMatchesFullName() {
        Doctor doctor = createDoctor("Jane", "Smith", cardiology);

        Page<Doctor> result = doctorRepository.searchDoctors(
                "jane smith",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(doctor);
    }

    @Test
    void shouldSearchDoctorsByDepartmentName_whenKeywordMatchesDepartment() {
        Doctor doctor = createDoctor("Robert", "Brown", neurology);

        Page<Doctor> result = doctorRepository.searchDoctors(
                "neurology",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(doctor);
    }

    @Test
    void shouldNotReturnDeletedDoctors_whenSearchDoctors() {
        Doctor doctor = createDoctor("Deleted", "Doctor", cardiology);
        doctor.setIsDeleted(true);
        doctorRepository.saveAndFlush(doctor);

        Page<Doctor> result = doctorRepository.searchDoctors(
                "deleted",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnDoctorsOrderedByCreatedAtDesc_whenFindByDepartmentId() {
        Doctor olderDoctor = createDoctor("Old", "Doctor", cardiology);
        olderDoctor.setCreatedAt(olderDoctor.getCreatedAt().minusDays(1));
        doctorRepository.saveAndFlush(olderDoctor);

        Doctor newerDoctor = createDoctor("New", "Doctor", cardiology);

        List<Doctor> result = doctorRepository
                .findByDepartmentIdAndIsDeletedFalseOrderByCreatedAtDesc(cardiology.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(newerDoctor.getId());
        assertThat(result.get(1).getId()).isEqualTo(olderDoctor.getId());
    }

    private Doctor createDoctor(String firstName, String lastName, Department department) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(UUID.randomUUID() + "@mail.com");
        user.setPassword("password");
        user.setRole(Role.DOCTOR);
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .department(department)
                .qualification("MBBS")
                .designation("Consultant")
                .specialization("Cardiology")
                .advanceBookingDays(30)
                .roomNumber("A101")
                .photoUrl("photo-url")
                .build();

        return doctorRepository.saveAndFlush(doctor);
    }
}