package org.example.eventpro.dto;

import lombok.*;
import org.example.eventpro.dto.enums.Role;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String organizationName;
}
