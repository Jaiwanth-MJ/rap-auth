package com.rapauth.rapauth.dto.user;

import com.rapauth.rapauth.entity.user.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String registerNumber;
    private UserRole role;
    private boolean active;
}