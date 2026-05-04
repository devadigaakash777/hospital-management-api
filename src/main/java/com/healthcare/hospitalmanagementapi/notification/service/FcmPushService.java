package com.healthcare.hospitalmanagementapi.notification.service;

import com.google.firebase.messaging.*;
import com.healthcare.hospitalmanagementapi.notification.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmPushService {

    private final UserDeviceTokenRepository deviceTokenRepository;

    public void sendToToken(String token, String title, String body, Map<String, String> data) {
        Message message = buildMessage(token, title, body, data);
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM sent to single token. Response: {}", response);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM send failed for token {}: {}", token, e.getMessage());
            if (isInvalidToken(e)) {
                deviceTokenRepository.deleteAllByFcmTokenIn(List.of(token));
                log.info("Removed invalid FCM token: {}", token);
            }
        }
    }

    @Transactional
    public void sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens.isEmpty()) {
            log.info("No FCM tokens to send to.");
            return;
        }

        List<String> invalidTokens = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i += 500) {
            List<String> chunk = tokens.subList(i, Math.min(i + 500, tokens.size()));

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(chunk)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null) {
                data.forEach(messageBuilder::putData);
            }

            MulticastMessage message = messageBuilder.build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                log.info("FCM multicast: {} success, {} failure out of {} tokens",
                        response.getSuccessCount(), response.getFailureCount(), chunk.size());

                List<SendResponse> results = response.getResponses();
                for (int j = 0; j < results.size(); j++) {
                    if (!results.get(j).isSuccessful()) {
                        FirebaseMessagingException ex = results.get(j).getException();
                        if (ex != null && isInvalidToken(ex)) {
                            invalidTokens.add(chunk.get(j));
                        }
                    }
                }

            } catch (FirebaseMessagingException e) {
                log.error("FCM multicast batch failed: {}", e.getMessage());
            }
        }

        if (!invalidTokens.isEmpty()) {
            deviceTokenRepository.deleteAllByFcmTokenIn(invalidTokens);
            log.info("Removed {} invalid FCM tokens", invalidTokens.size());
        }
    }

    private Message buildMessage(String token, String title, String body, Map<String, String> data) {
        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());

        if (data != null) {
            data.forEach(builder::putData);
        }

        return builder.build();
    }

    private boolean isInvalidToken(FirebaseMessagingException e) {
        MessagingErrorCode code = e.getMessagingErrorCode();
        return code == MessagingErrorCode.UNREGISTERED
                || code == MessagingErrorCode.INVALID_ARGUMENT
                || code == MessagingErrorCode.SENDER_ID_MISMATCH;
    }
}