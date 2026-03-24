package com.healthcare.hospitalmanagementapi.auth.service;

import com.healthcare.hospitalmanagementapi.auth.dto.*;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO request);

    AuthResponseDTO refreshToken(RefreshTokenRequestDTO request);

    void logout(String refreshToken);
}