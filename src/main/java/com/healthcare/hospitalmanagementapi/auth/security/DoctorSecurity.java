package com.healthcare.hospitalmanagementapi.auth.security;

import com.healthcare.hospitalmanagementapi.doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("doctorSecurity")
@RequiredArgsConstructor
public class DoctorSecurity {

    private final DoctorRepository doctorRepository;

    public boolean isSelfOrAdmin(UUID doctorId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isDoctor = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (isAdmin) {
            return true;
        }

        if (!isDoctor) {
            return false;
        }

        UUID loggedInUserId = userDetails.getUser().getId();

        return doctorRepository.findByIdAndIsDeletedFalse(doctorId)
                .map(doctor -> doctor.getUser().getId().equals(loggedInUserId))
                .orElse(false);
    }
}
