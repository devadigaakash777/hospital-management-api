package com.healthcare.hospitalmanagementapi.security.service;

import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogRequestDTO;
import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SuspiciousActivityService {

    void logEvent(SuspiciousActivityLogRequestDTO request);

    Page<SuspiciousActivityLogResponseDTO> getLogs(
            String email,
            String ipAddress,
            SuspiciousEvent eventType,
            Pageable pageable
    );

    boolean recordLoginFailureAndCheck(String email);

    void clearLoginFailures(String email);
}