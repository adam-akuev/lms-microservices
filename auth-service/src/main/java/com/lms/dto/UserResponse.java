package com.lms.dto;

import com.lms.model.User;

public record UserResponse(
        Long id,
        String fullName,
        String phoneNumber,
        String email
)
{
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getEmail()
        );
    }
}
