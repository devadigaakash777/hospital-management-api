package com.healthcare.hospitalmanagementapi.unit.user.repository;

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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Transactional
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    private User activeUser;
    private User deletedUser;

    @BeforeEach
    void setUp() {
        activeUser = createUser("John", "Doe", "john.doe@mail.com");
        deletedUser = createUser("Jane", "Smith", "jane.smith@mail.com");
        deletedUser.setIsDeleted(true);
        deletedUser = userRepository.saveAndFlush(deletedUser);
    }

    @Test
    void shouldReturnUser_whenFindByIdAndIsDeletedFalse() {
        Optional<User> result = userRepository.findByIdAndIsDeletedFalse(activeUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeUser.getId());
    }

    @Test
    void shouldNotReturnDeletedUser_whenFindByIdAndIsDeletedFalse() {
        Optional<User> result = userRepository.findByIdAndIsDeletedFalse(deletedUser.getId());

        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnUser_whenFindByEmailAndIsDeletedFalse() {
        Optional<User> result = userRepository.findByEmailAndIsDeletedFalse(activeUser.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(activeUser.getEmail());
    }

    @Test
    void shouldNotReturnDeletedUser_whenFindByEmailAndIsDeletedFalse() {
        Optional<User> result = userRepository.findByEmailAndIsDeletedFalse(deletedUser.getEmail());

        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnUserIncludingDeleted_whenFindByEmail() {
        Optional<User> result = userRepository.findByEmail(deletedUser.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(deletedUser.getId());
        assertThat(result.get().getIsDeleted()).isTrue();
    }

    @Test
    void shouldReturnTrue_whenExistsByEmailAndIsDeletedFalse() {
        boolean exists = userRepository.existsByEmailAndIsDeletedFalse(activeUser.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenDeletedUserExistsByEmailAndIsDeletedFalse() {
        boolean exists = userRepository.existsByEmailAndIsDeletedFalse(deletedUser.getEmail());

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnOnlyNonDeletedUsers_whenFindAllByIsDeletedFalse() {
        Page<User> result = userRepository.findAllByIsDeletedFalse(PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(User::getId)
                .contains(activeUser.getId())
                .doesNotContain(deletedUser.getId());
    }

    @Test
    void shouldSearchUsersByFirstName_whenKeywordMatchesFirstName() {
        Page<User> result = userRepository.searchUsers(
                "john",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(activeUser);
    }

    @Test
    void shouldSearchUsersByLastName_whenKeywordMatchesLastName() {
        Page<User> result = userRepository.searchUsers(
                "doe",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(activeUser);
    }

    @Test
    void shouldSearchUsersByEmail_whenKeywordMatchesEmail() {
        Page<User> result = userRepository.searchUsers(
                "john.doe",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(activeUser);
    }

    @Test
    void shouldNotReturnDeletedUsers_whenSearchUsers() {
        Page<User> result = userRepository.searchUsers(
                "jane",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    private User createUser(String firstName, String lastName, String email) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password("password")
                .role(Role.ADMIN)
                .build();

        return userRepository.saveAndFlush(user);
    }
}