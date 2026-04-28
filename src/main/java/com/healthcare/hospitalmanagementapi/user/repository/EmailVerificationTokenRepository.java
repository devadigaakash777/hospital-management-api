package com.healthcare.hospitalmanagementapi.user.repository;

import com.healthcare.hospitalmanagementapi.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByEmail(String email);

    Optional<EmailVerificationToken> findByEmailAndOtp(String email, String otp);

    boolean existsByEmail(String email);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    void deleteAllExpired(LocalDateTime now);
}