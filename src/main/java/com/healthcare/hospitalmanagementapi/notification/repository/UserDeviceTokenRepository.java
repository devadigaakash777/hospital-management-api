package com.healthcare.hospitalmanagementapi.notification.repository;

import com.healthcare.hospitalmanagementapi.notification.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, UUID> {

    List<UserDeviceToken> findAllByUserId(UUID userId);

    @Query("SELECT t FROM UserDeviceToken t WHERE t.user.id IN :userIds")
    List<UserDeviceToken> findAllByUserIdIn(@Param("userIds") List<UUID> userIds);

    Optional<UserDeviceToken> findByFcmToken(String fcmToken);

    boolean existsByFcmToken(String fcmToken);

    @Modifying
    @Query("DELETE FROM UserDeviceToken t WHERE t.fcmToken IN :tokens")
    void deleteAllByFcmTokenIn(@Param("tokens") List<String> tokens);
}