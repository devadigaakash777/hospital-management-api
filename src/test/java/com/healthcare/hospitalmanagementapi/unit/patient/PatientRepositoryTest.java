package com.healthcare.hospitalmanagementapi.unit.patient;

import com.healthcare.hospitalmanagementapi.patient.entity.Patient;
import com.healthcare.hospitalmanagementapi.patient.repository.PatientRepository;
import jakarta.transaction.Transactional;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Transactional
class PatientRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void shouldFindPatientByIdAndIsDeletedFalse_whenPatientIsActive() {
        Patient patient = createPatient("Akash", "Devadiga", "UH-2026-0001");

        Optional<Patient> result = patientRepository.findByIdAndIsDeletedFalse(patient.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(patient.getId());
    }

    @Test
    void shouldReturnEmpty_whenFindByIdAndIsDeletedFalseAndPatientIsDeleted() {
        Patient patient = createPatient("Akash", "Devadiga", "UH-2026-0001");

        patientRepository.delete(patient);

        Optional<Patient> result = patientRepository.findByIdAndIsDeletedFalse(patient.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSearchPatientsByFirstName_whenKeywordMatches() {
        Patient patient = createPatient("Akash", "Devadiga", "UH-2026-0001");

        Page<Patient> result = patientRepository.search(
                "akash",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(patient);
    }

    @Test
    void shouldSearchPatientsByLastName_whenKeywordMatches() {
        Patient patient = createPatient("Akash", "Devadiga", "UH-2026-0001");

        Page<Patient> result = patientRepository.search(
                "devadiga",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(patient);
    }

    @Test
    void shouldSearchPatientsByUhId_whenKeywordMatches() {
        Patient patient = createPatient("Akash", "Devadiga", "UH-2026-0001");

        Page<Patient> result = patientRepository.search(
                "2026-0001",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(patient);
    }

    @Test
    void shouldNotReturnDeletedPatients_whenSearching() {
        Patient patient = createPatient("Deleted", "Patient", "UH-2026-0009");

        patientRepository.delete(patient);

        Page<Patient> result = patientRepository.search(
                "Deleted",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnLatestUhIdByPrefix_whenRecordsExist() {
        patientRepository.saveAndFlush(createPatient("A", "One", "UH-2026-0001"));
        patientRepository.saveAndFlush(createPatient("B", "Two", "UH-2026-0012"));
        patientRepository.saveAndFlush(createPatient("C", "Three", "UH-2026-0009"));

        Optional<String> result = patientRepository.findLatestUhIdByPrefix("UH-2026-");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("UH-2026-0012");
    }

    @Test
    void shouldReturnEmpty_whenNoUhIdExistsForPrefix() {
        Optional<String> result = patientRepository.findLatestUhIdByPrefix("UH-2030-");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnDeletedPatient_whenFindByIdIncludingDeleted() {
        Patient patient = createPatient("Akash", "Devadiga", "UH-2026-0001");

        patientRepository.delete(patient);

        Optional<Patient> result = patientRepository.findByIdIncludingDeleted(patient.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getIsDeleted()).isTrue();
    }

    private Patient createPatient(String firstName, String lastName, String uhId) {
        Patient patient = Patient.builder()
                .firstName(firstName)
                .lastName(lastName)
                .uhId(uhId)
                .phoneNumber("+919999999999")
                .email(UUID.randomUUID() + "@mail.com")
                .build();

        return patientRepository.saveAndFlush(patient);
    }
}