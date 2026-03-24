package com.healthcare.hospitalmanagementapi.auth.service.impl;

import com.healthcare.hospitalmanagementapi.auth.dto.*;
import com.healthcare.hospitalmanagementapi.auth.entity.RefreshToken;
import com.healthcare.hospitalmanagementapi.auth.service.AuthService;
import com.healthcare.hospitalmanagementapi.auth.service.RefreshTokenService;
import com.healthcare.hospitalmanagementapi.auth.util.JwtUtil;
import com.healthcare.hospitalmanagementapi.common.exception.custom.UnauthorizedException;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String accessToken = jwtUtil.generateAccessToken(user);
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
        RefreshToken newRefresh = refreshTokenService.createRefreshToken(user);

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefresh.getRefreshToken())
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}