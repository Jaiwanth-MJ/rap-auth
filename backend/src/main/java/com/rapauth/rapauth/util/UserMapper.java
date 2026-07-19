package com.rapauth.rapauth.util;

import com.rapauth.rapauth.dto.user.UserResponse;
import com.rapauth.rapauth.entity.user.User;



public class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .registerNumber(user.getRegisterNumber())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }
}