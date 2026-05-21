package com.healthcare.hospitalmanagementapi.unit.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String LOGIN_URL = "/api/v1/auth/login";

    private static final String LOGIN_BODY = """
            {
              "email": "test@gmail.com",
              "password": "wrongpassword"
            }
            """;

    // ─── Auth Endpoints ────────────────────────────────────────────────────────

    @Test
    void shouldAllowRequest_whenAuthLimitNotExceeded() throws Exception {
        for (int i = 0; i < 5; i++) {
            int status = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LOGIN_BODY)
                            .with(request -> {
                                request.setRemoteAddr("10.0.0.1");
                                return request;
                            }))
                    .andReturn().getResponse().getStatus();

            assertThat(status).isNotEqualTo(429);
        }
    }

    @Test
    void shouldBlockRequest_whenAuthLimitExceeded() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(LOGIN_BODY)
                    .with(request -> {
                        request.setRemoteAddr("10.0.0.2");
                        return request;
                    }));
        }

        // 6th should be blocked
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY)
                        .with(request -> {
                            request.setRemoteAddr("10.0.0.2");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_REQUESTS"))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Try after sometime."));
    }

    // ─── IP Isolation ──────────────────────────────────────────────────────────

    @Test
    void shouldTrackLimitsSeparately_whenDifferentIps() throws Exception {
        // IP 1 — exhaust limit
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(LOGIN_BODY)
                    .with(request -> {
                        request.setRemoteAddr("30.0.0.1");
                        return request;
                    }));
        }

        // IP 1 — should be blocked
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY)
                        .with(request -> {
                            request.setRemoteAddr("30.0.0.1");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests());

        // IP 2 — should still be allowed
        int status = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY)
                        .with(request -> {
                            request.setRemoteAddr("30.0.0.2");
                            return request;
                        }))
                .andReturn().getResponse().getStatus();

        assertThat(status).isNotEqualTo(429);
    }

    @Test
    void shouldTrackAuthAndGeneralLimitsSeparately_whenSameIp() throws Exception {
        // Same IP — exhaust auth limit
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(LOGIN_BODY)
                    .with(request -> {
                        request.setRemoteAddr("40.0.0.1");
                        return request;
                    }));
        }

        // Auth — should be blocked
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY)
                        .with(request -> {
                            request.setRemoteAddr("40.0.0.1");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests());

        // Same IP — actuator endpoint uses general bucket, should still work
        int status = mockMvc.perform(post("/actuator/health")
                        .with(request -> {
                            request.setRemoteAddr("40.0.0.1");
                            return request;
                        }))
                .andReturn().getResponse().getStatus();

        assertThat(status).isNotEqualTo(429);
    }

    // ─── X-Forwarded-For ───────────────────────────────────────────────────────

    @Test
    void shouldUseXForwardedForHeader_whenPresent() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(LOGIN_BODY)
                    .header("X-Forwarded-For", "50.0.0.1, 192.168.1.1"));
        }

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY)
                        .header("X-Forwarded-For", "50.0.0.1, 192.168.1.1"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldFallbackToRemoteAddr_whenXForwardedForAbsent() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(LOGIN_BODY)
                    .with(request -> {
                        request.setRemoteAddr("60.0.0.1");
                        return request;
                    }));
        }

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY)
                        .with(request -> {
                            request.setRemoteAddr("60.0.0.1");
                            return request;
                        }))
                .andExpect(status().isTooManyRequests());
    }
}