package com.healthcare.hospitalmanagementapi.auth.service.impl;
import com.healthcare.hospitalmanagementapi.auth.entity.RefreshToken;
import com.healthcare.hospitalmanagementapi.auth.repository.RefreshTokenRepository;
import com.healthcare.hospitalmanagementapi.auth.service.RefreshTokenService;
import com.healthcare.hospitalmanagementapi.common.exception.custom.UnauthorizedException;
import com.healthcare.hospitalmanagementapi.config.JwtProperties;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtProperties jwtProperties;

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .refreshToken(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
                .createdAt(Instant.now())
                .build();

        return repository.save(token);
    }

    @Override
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = repository.findByTokenWithUser(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {

        RefreshToken refreshToken = repository.findByRefreshTokenAndIsRevokedFalse(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        repository.save(refreshToken);
    }

    @Override
    public void deleteExpiredTokens() {
        repository.deleteByExpiryDateBefore(Instant.now());
    }
}