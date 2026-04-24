package com.healthcare.hospitalmanagementapi.unit.healthpackageappointment;

import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageAppointmentRepository;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Transactional
class HealthPackageAppointmentRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private HealthPackageAppointmentRepository repository;

    @Test
    void shouldReturnNextTokenNumber() {
        Integer token = repository.getNextTokenNumber(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now().plusDays(1)
        );

        assertThat(token).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldReturnZero_whenNoAppointments() {
        long count = repository.countActiveAppointmentsForSlot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                AppointmentStatus.CANCELLED
        );

        assertThat(count).isZero();
    }
}