package com.healthcare.hospitalmanagementapi.user.dto.group;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupSummaryDTO {
    private UUID id;
    private String groupName;
}