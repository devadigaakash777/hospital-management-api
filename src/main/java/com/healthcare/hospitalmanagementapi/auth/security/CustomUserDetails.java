package com.healthcare.hospitalmanagementapi.auth.security;

import com.healthcare.hospitalmanagementapi.enums.Role;
import com.healthcare.hospitalmanagementapi.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        if (Boolean.TRUE.equals(user.getCanManageDoctorSlots()))
            authorities.add(new SimpleGrantedAuthority("CAN_MANAGE_DOCTOR_SLOTS"));

        if (Boolean.TRUE.equals(user.getCanManageStaff()))
            authorities.add(new SimpleGrantedAuthority("CAN_MANAGE_STAFF"));

        if (Boolean.TRUE.equals(user.getCanManageGroups()))
            authorities.add(new SimpleGrantedAuthority("CAN_MANAGE_GROUPS"));

        if (Boolean.TRUE.equals(user.getCanExportReports()))
            authorities.add(new SimpleGrantedAuthority("CAN_EXPORT_REPORTS"));

        if(Boolean.TRUE.equals(user.getCanManageHealthPackages()))
            authorities.add(new SimpleGrantedAuthority("CAN_MANAGE_HEALTH_PACKAGES"));

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return user.getRole();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return !Boolean.TRUE.equals(user.getIsDeleted()); }
}