package com.healthcare.hospitalmanagementapi.auth.service.impl;

import com.healthcare.hospitalmanagementapi.auth.entity.RefreshToken;
import com.healthcare.hospitalmanagementapi.auth.repository.RefreshTokenRepository;
import com.healthcare.hospitalmanagementapi.auth.service.RefreshTokenService;
import com.healthcare.hospitalmanagementapi.common.exception.custom.UnauthorizedException;
import com.healthcare.hospitalmanagementapi.auth.util.RequestMetadataExtractor;
import com.healthcare.hospitalmanagementapi.config.JwtProperties;
import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogRequestDTO;
import com.healthcare.hospitalmanagementapi.security.service.SuspiciousActivityService;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository    repository;
    private final JwtProperties             jwtProperties;
    private final SuspiciousActivityService suspiciousActivityService;
    private final RequestMetadataExtractor  requestMetadataExtractor;

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
        Optional<RefreshToken> maybeToken = repository.findByRefreshToken(token);

        if (maybeToken.isEmpty()) {
            suspiciousActivityService.logEvent(
                    SuspiciousActivityLogRequestDTO.builder()
                            .eventType(SuspiciousEvent.INVALID_TOKEN_ATTEMPT)
                            .email(null)
                            .ipAddress(requestMetadataExtractor.getClientIp())
                            .userAgent(requestMetadataExtractor.getUserAgent())
                            .tokenHint(safeHint(token))
                            .detail("Token not found in database")
                            .build()
            );
            throw new UnauthorizedException("Invalid refresh token");
        }

        RefreshToken refreshToken = maybeToken.get();

        if (refreshToken.isRevoked()) {
            revokeAllActiveTokensForUser(refreshToken.getUser());

            suspiciousActivityService.logEvent(
                    SuspiciousActivityLogRequestDTO.builder()
                            .eventType(SuspiciousEvent.REVOKED_TOKEN_REUSE)
                            .email(refreshToken.getUser().getEmail())
                            .ipAddress(requestMetadataExtractor.getClientIp())
                            .userAgent(requestMetadataExtractor.getUserAgent())
                            .tokenHint(safeHint(token))
                            .detail("Revoked token reused — all active sessions wiped for user "
                                    + refreshToken.getUser().getId())
                            .build()
            );
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            suspiciousActivityService.logEvent(
                    SuspiciousActivityLogRequestDTO.builder()
                            .eventType(SuspiciousEvent.EXPIRED_TOKEN_REUSE)
                            .email(refreshToken.getUser().getEmail())
                            .ipAddress(requestMetadataExtractor.getClientIp())
                            .userAgent(requestMetadataExtractor.getUserAgent())
                            .tokenHint(safeHint(token))
                            .detail("Expired refresh token submitted")
                            .build()
            );
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
    public void revokeRefreshTokenIfPresent(String token) {
        repository.findByRefreshTokenAndIsRevokedFalse(token)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    repository.save(rt);
                });
    }

    @Override
    public void deleteExpiredTokens() {
        repository.deleteByExpiryDateBefore(Instant.now());
    }

    private void revokeAllActiveTokensForUser(User user) {
        List<RefreshToken> active = repository.findAllByUser_IdAndIsRevokedFalse(user.getId());
        active.forEach(t -> t.setRevoked(true));
        repository.saveAll(active);
    }

    private String safeHint(String token) {
        if (token == null || token.isBlank()) return null;
        return token.substring(0, Math.min(8, token.length()));
    }
}