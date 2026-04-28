package com.healthcare.hospitalmanagementapi.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("Your account verification code");
            msg.setText("""
                    Hello,

                    An administrator has created an account for you.
                    Your email verification code is:

                         %s

                    This code expires in 10 minutes.
                    Do not share it with anyone.

                    — Hospital Management System
                    """.formatted(otp));

            mailSender.send(msg);
            log.info("OTP email sent to {}", toEmail);

        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
        }
    }

    @Async
    public void sendBulkEmail(List<String> emails, String subject, String message) {
        for (String email : emails) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(email);
                msg.setSubject(subject);
                msg.setText(message);

                mailSender.send(msg);
                log.info("Email sent to {}", email);

            } catch (Exception ex) {
                log.error("Failed to send email to {}: {}", email, ex.getMessage());
            }
        }
    }
}