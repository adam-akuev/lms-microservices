package com.lms.dto.internal;

public record UserInternalResponse(
        Long id,
        String fullName,
        String email
) {}