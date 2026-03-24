package com.healthcare.hospitalmanagementapi.user.repository;

import com.healthcare.hospitalmanagementapi.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndIsDeletedFalse(String email);

    Page<User> findAllByIsDeletedFalse(Pageable pageable);

}