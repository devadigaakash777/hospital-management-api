package com.healthcare.hospitalmanagementapi.unit.appointment;

import com.healthcare.hospitalmanagementapi.appointment.entity.Appointment;
import com.healthcare.hospitalmanagementapi.appointment.repository.AppointmentRepository;
import com.healthcare.hospitalmanagementapi.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Transactional
class AppointmentRepositoryTest {

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
    private AppointmentRepository appointmentRepository;

    @Test
    void shouldReturnAppointmentIncludingDeleted_whenFindByIdIncludingDeleted() {
        UUID id = UUID.randomUUID();

        Optional<Appointment> result = appointmentRepository.findByIdIncludingDeleted(id);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnNextTokenNumber_whenAppointmentsAlreadyExist() {
        Integer token = appointmentRepository.getNextTokenNumber(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now().plusDays(1)
        );

        assertThat(token).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldReturnZeroCount_whenNoAppointmentsForSlot() {
        long count = appointmentRepository.countActiveAppointmentsForSlot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                false,
                AppointmentStatus.CANCELLED
        );

        assertThat(count).isZero();
    }
}