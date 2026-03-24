package com.healthcare.hospitalmanagementapi.auth.service;

import com.healthcare.hospitalmanagementapi.auth.entity.RefreshToken;
import com.healthcare.hospitalmanagementapi.user.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken validateRefreshToken(String token);

    void revokeRefreshToken(String token);

    void deleteExpiredTokens();
}