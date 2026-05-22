package com.healthcare.hospitalmanagementapi.auth.service.impl;

import com.healthcare.hospitalmanagementapi.auth.dto.AuthResponseDTO;
import com.healthcare.hospitalmanagementapi.auth.dto.LoginRequestDTO;
import com.healthcare.hospitalmanagementapi.auth.dto.RefreshTokenRequestDTO;
import com.healthcare.hospitalmanagementapi.auth.entity.RefreshToken;
import com.healthcare.hospitalmanagementapi.auth.service.AuthService;
import com.healthcare.hospitalmanagementapi.auth.service.RefreshTokenService;
import com.healthcare.hospitalmanagementapi.auth.util.JwtUtil;
import com.healthcare.hospitalmanagementapi.common.exception.custom.UnauthorizedException;
import com.healthcare.hospitalmanagementapi.auth.util.RequestMetadataExtractor;
import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogRequestDTO;
import com.healthcare.hospitalmanagementapi.security.service.SuspiciousActivityService;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager     authenticationManager;
    private final UserRepository            userRepository;
    private final JwtUtil                   jwtUtil;
    private final RefreshTokenService       refreshTokenService;
    private final SuspiciousActivityService suspiciousActivityService;
    private final RequestMetadataExtractor  requestMetadataExtractor;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException | DisabledException ex) {
            boolean isBruteForce = suspiciousActivityService.recordLoginFailureAndCheck(request.getEmail());

            suspiciousActivityService.logEvent(
                    SuspiciousActivityLogRequestDTO.builder()
                            .eventType(isBruteForce
                                    ? SuspiciousEvent.BRUTE_FORCE_DETECTED
                                    : SuspiciousEvent.LOGIN_FAILURE)
                            .email(request.getEmail())
                            .ipAddress(requestMetadataExtractor.getClientIp())
                            .userAgent(requestMetadataExtractor.getUserAgent())
                            .tokenHint(null)
                            .detail(ex.getClass().getSimpleName() + ": " + ex.getMessage())
                            .build()
            );

            throw new UnauthorizedException("Invalid credentials");
        }

        suspiciousActivityService.clearLoginFailures(request.getEmail());

        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String accessToken        = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .tokenType("Bearer")
                .build();
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        String newAccessToken = jwtUtil.generateAccessToken(user);

        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getRefreshToken())
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshTokenIfPresent(refreshToken);
    }
}