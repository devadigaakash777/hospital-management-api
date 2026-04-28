package com.healthcare.hospitalmanagementapi.support;

import com.healthcare.hospitalmanagementapi.auth.security.CustomUserDetails;
import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockAdminSecurityContextFactory
        implements WithSecurityContextFactory<WithMockAdmin> {

    @Override
    public SecurityContext createSecurityContext(WithMockAdmin annotation) {
        User adminUser = new User();
        adminUser.setEmail(annotation.email());
        adminUser.setRole(Role.ADMIN);
        adminUser.setCanManageStaff(true);
        adminUser.setCanManageDoctorSlots(true);
        adminUser.setPassword("irrelevant");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");

        CustomUserDetails userDetails = new CustomUserDetails(adminUser);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}