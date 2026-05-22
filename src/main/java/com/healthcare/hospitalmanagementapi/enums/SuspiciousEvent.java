package com.healthcare.hospitalmanagementapi.enums;

public enum SuspiciousEvent {

    /**
     * A refresh token that was already rotated/revoked was reused.
     * Strongest signal of token theft — a legitimate user never replays a revoked token.
     */
    REVOKED_TOKEN_REUSE,

    /**
     * A refresh token that has passed its expiry date was submitted.
     */
    EXPIRED_TOKEN_REUSE,

    /**
     * A token string that does not exist in the database at all was submitted.
     * Could be a forged, tampered, or already-deleted token.
     */
    INVALID_TOKEN_ATTEMPT,

    /**
     * A single failed login attempt (wrong email or password).
     */
    LOGIN_FAILURE,

    /**
     * Login failure threshold crossed within the rolling window — likely brute force.
     */
    BRUTE_FORCE_DETECTED
}