package com.healthcare.hospitalmanagementapi.auth.util;

import com.healthcare.hospitalmanagementapi.config.JwtProperties;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("authorities", getAuthorities(user))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    private List<String> getAuthorities(User user) {
        List<String> list = new ArrayList<>();

        if (Boolean.TRUE.equals(user.getCanManageDoctorSlots()))
            list.add("CAN_MANAGE_DOCTOR_SLOTS");

        if (Boolean.TRUE.equals(user.getCanManageStaff()))
            list.add("CAN_MANAGE_STAFF");

        if (Boolean.TRUE.equals(user.getCanManageGroups()))
            list.add("CAN_MANAGE_GROUPS");

        return list;
    }
}