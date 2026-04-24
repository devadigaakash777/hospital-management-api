package com.healthcare.hospitalmanagementapi.unit.healthpackage;

import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackage;
import com.healthcare.hospitalmanagementapi.healthpackage.repository.HealthPackageRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Transactional
class HealthPackageRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private HealthPackageRepository repository;

    private HealthPackage createPackage(String name, boolean active) {
        HealthPackage hp = new HealthPackage();
        hp.setPackageName(name);
        hp.setPackagePrice(BigDecimal.TEN);
        hp.setAdvanceBookingDays(30);
        hp.setIsActive(active);
        return repository.saveAndFlush(hp);
    }

    @Test
    void shouldFindByIdIncludingDeleted() {
        HealthPackage hp = createPackage("Test", true);

        Optional<HealthPackage> result = repository.findByIdIncludingDeleted(hp.getId());

        assertThat(result).isPresent();
    }

    @Test
    void shouldSearchHealthPackages_byName() {
        HealthPackage hp = createPackage("Heart Checkup", true);

        Page<HealthPackage> result =
                repository.searchHealthPackages("heart", PageRequest.of(0,10));

        assertThat(result.getContent()).extracting("id").contains(hp.getId());
    }

    @Test
    void shouldSearchHealthPackages_byDescription() {
        HealthPackage hp = createPackage("Full Body", true);
        hp.setDescription("Blood test included");
        repository.saveAndFlush(hp);

        Page<HealthPackage> result =
                repository.searchHealthPackages("blood", PageRequest.of(0,10));

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void shouldReturnOnlyActiveAndNotDeleted_inSearch() {
        HealthPackage inactive = createPackage("Inactive", false);

        Page<HealthPackage> result =
                repository.searchHealthPackages("inactive", PageRequest.of(0,10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnAllIncludingDeleted() {
        HealthPackage hp = createPackage("Test", true);

        Page<HealthPackage> result =
                repository.findAllIncludingDeleted(PageRequest.of(0,10));

        assertThat(result.getContent()).isNotEmpty();
    }
}