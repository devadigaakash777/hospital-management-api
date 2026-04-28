package com.healthcare.hospitalmanagementapi.auth.repository;

import com.healthcare.hospitalmanagementapi.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.refreshToken = :token AND rt.isRevoked = false")
    Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);

    Optional<RefreshToken> findByRefreshTokenAndIsRevokedFalse(String token);

    void deleteByExpiryDateBefore(Instant now);

    List<RefreshToken> findAllByUser_Id(UUID userId);
}