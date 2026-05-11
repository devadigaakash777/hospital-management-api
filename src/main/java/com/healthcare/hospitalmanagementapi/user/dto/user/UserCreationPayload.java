package com.healthcare.hospitalmanagementapi.user.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationPayload {

    private CreateUserRequestDTO dto;
    private String temporaryPassword;
}