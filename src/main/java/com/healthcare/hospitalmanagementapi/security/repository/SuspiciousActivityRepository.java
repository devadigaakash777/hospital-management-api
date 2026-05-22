package com.healthcare.hospitalmanagementapi.security.repository;

import com.healthcare.hospitalmanagementapi.security.entity.SuspiciousActivityLog;
import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface SuspiciousActivityRepository extends JpaRepository<SuspiciousActivityLog, UUID> {

    @Query(value = """
            SELECT s.*
            FROM suspicious_activity_logs s
            WHERE (:email      IS NULL OR s.email      = :email)
              AND (:ipAddress  IS NULL OR s.ip_address = :ipAddress)
              AND (CAST(:eventType AS suspicious_event_type) IS NULL
                   OR s.event_type = CAST(:eventType AS suspicious_event_type))
            ORDER BY s.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(s.id)
            FROM suspicious_activity_logs s
            WHERE (:email      IS NULL OR s.email      = :email)
              AND (:ipAddress  IS NULL OR s.ip_address = :ipAddress)
              AND (CAST(:eventType AS suspicious_event_type) IS NULL
                   OR s.event_type = CAST(:eventType AS suspicious_event_type))
            """,
            nativeQuery = true)
    Page<SuspiciousActivityLog> findByFilters(
            @Param("email")      String email,
            @Param("ipAddress")  String ipAddress,
            @Param("eventType")  String eventType,
            Pageable pageable
    );

    @Query(value = """
            SELECT COUNT(s.id)
            FROM suspicious_activity_logs s
            WHERE s.ip_address = :ipAddress
              AND s.event_type = ANY(CAST(:eventTypes AS suspicious_event_type[]))
              AND s.created_at >= :since
            """,
            nativeQuery = true)
    long countByIpAddressAndEventTypeInAndCreatedAtGreaterThanEqual(
            @Param("ipAddress")  String ipAddress,
            @Param("eventTypes") String eventTypes,
            @Param("since")      Instant since
    );
}