package com.healthcare.hospitalmanagementapi.user.service.impl;

import com.healthcare.hospitalmanagementapi.common.exception.custom.BadRequestException;
import com.healthcare.hospitalmanagementapi.user.dto.password.ForgotPasswordRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.password.ResetPasswordRequestDTO;
import com.healthcare.hospitalmanagementapi.user.entity.EmailVerificationToken;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import com.healthcare.hospitalmanagementapi.user.repository.EmailVerificationTokenRepository;
import com.healthcare.hospitalmanagementapi.user.repository.UserRepository;
import com.healthcare.hospitalmanagementapi.user.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String FORGOT_PASSWORD_MARKER = "FORGOT_PASSWORD";

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;


    @Override
    public void initiateForgotPassword(ForgotPasswordRequestDTO dto) {

        String email = dto.getEmail().toLowerCase().trim();

        Optional<User> userOpt = userRepository.findByEmailAndIsDeletedFalse(email);

        if (userOpt.isEmpty()) {
            log.warn("Forgot-password requested for unknown/deleted email: {}", email);
            return;
        }

        tokenRepository.deleteByEmail(email);

        String otp = generateOtp();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .otp(otp)
                .payload(FORGOT_PASSWORD_MARKER)   // type discriminator
                .userId(userOpt.get().getId())      // links token to the user
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        tokenRepository.save(token);
        emailService.sendPasswordResetOtpEmail(email, otp);

        log.info("Password-reset OTP sent to {}", email);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO dto) {

        String email = dto.getEmail().toLowerCase().trim();

        EmailVerificationToken token = tokenRepository
                .findByEmailAndOtp(email, dto.getOtp())
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        // Make sure this token is a password-reset token, not a registration one.
        if (!FORGOT_PASSWORD_MARKER.equals(token.getPayload())) {
            throw new BadRequestException("Invalid OTP");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.deleteByEmail(email);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        tokenRepository.deleteByEmail(email);

        log.info("Password successfully reset for user {}", user.getId());
    }

    private String generateOtp() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
    }
}