package com.healthcare.hospitalmanagementapi.security.entity;

import com.healthcare.hospitalmanagementapi.enums.SuspiciousEvent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "suspicious_activity_logs",
        indexes = {
                @Index(name = "idx_sus_email",      columnList = "email"),
                @Index(name = "idx_sus_ip",         columnList = "ip_address"),
                @Index(name = "idx_sus_event_type", columnList = "event_type"),
                @Index(name = "idx_sus_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspiciousActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "event_type", nullable = false)
    private SuspiciousEvent eventType;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "token_hint", length = 8)
    private String tokenHint;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}