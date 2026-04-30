package com.healthcare.hospitalmanagementapi.user.service;

import com.healthcare.hospitalmanagementapi.user.dto.password.ForgotPasswordRequestDTO;
import com.healthcare.hospitalmanagementapi.user.dto.password.ResetPasswordRequestDTO;

public interface PasswordResetService {
    void initiateForgotPassword(ForgotPasswordRequestDTO dto);
    void resetPassword(ResetPasswordRequestDTO dto);
}