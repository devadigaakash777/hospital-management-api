package com.healthcare.hospitalmanagementapi.security.service.impl;

import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogRequestDTO;
import com.healthcare.hospitalmanagementapi.security.dto.SuspiciousActivityLogResponseDTO;
import com.healthcare.hospitalmanagementapi.security.entity.SuspiciousActivityLog;
import com.healthcare.hospitalmanagementapi.security.mapper.SuspiciousActivityLogMapper;
import com.healthcare.hospitalmanagementapi.security.repository.SuspiciousActivityRepository;
import com.healthcare.hospitalmanagementapi.security.service.SuspiciousActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuspiciousActivityServiceImpl implements SuspiciousActivityService {

    private static final int BRUTE_FORCE_THRESHOLD = 5;

    private final SuspiciousActivityRepository repository;
    private final SuspiciousActivityLogMapper  mapper;

    private final Map<String, AtomicInteger> loginFailureCount = new ConcurrentHashMap<>();

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(SuspiciousActivityLogRequestDTO request) {
        log.warn("[SECURITY] type={} email={} ip={} tokenHint={} detail={}",
                request.getEventType(),
                request.getEmail(),
                request.getIpAddress(),
                request.getTokenHint(),
                request.getDetail());

        SuspiciousActivityLog entry = mapper.toEntity(request);
        entry.setCreatedAt(Instant.now());

        repository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SuspiciousActivityLogResponseDTO> getLogs(
            String email,
            String ipAddress,
            SuspiciousEvent eventType,
            Pageable pageable
    ) {
        String eventTypeStr = (eventType != null) ? eventType.name() : null;
        return repository
                .findByFilters(email, ipAddress, eventTypeStr, pageable)
                .map(mapper::toResponseDTO);
    }

    @Override
    public boolean recordLoginFailureAndCheck(String email) {
        AtomicInteger count = loginFailureCount.computeIfAbsent(email, k -> new AtomicInteger(0));
        return count.incrementAndGet() >= BRUTE_FORCE_THRESHOLD;
    }

    @Override
    public void clearLoginFailures(String email) {
        loginFailureCount.remove(email);
    }
}