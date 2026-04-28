package com.healthcare.hospitalmanagementapi.user.scheduler;

import com.healthcare.hospitalmanagementapi.user.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupScheduler {

    private final EmailVerificationTokenRepository tokenRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredTokens() {
        tokenRepository.deleteAllExpired(LocalDateTime.now());
        log.info("Expired OTP tokens cleaned up");
    }
}